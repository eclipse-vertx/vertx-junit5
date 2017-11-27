/*
 * Copyright (c) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.ext.junit5;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public final class VertxTestContext {

  private Throwable throwableReference = null;
  private final CountDownLatch releaseLatch = new CountDownLatch(1);
  private final HashSet<Checkpoint> checkpoints = new HashSet<>();

  // ........................................................................................... //

  public boolean failed() {
    return throwableReference != null;
  }

  public Throwable causeOfFailure() {
    return throwableReference;
  }

  public boolean completed() {
    return !failed();
  }

  // ........................................................................................... //

  public void completeNow() {
    releaseLatch.countDown();
  }

  public synchronized void failNow(Throwable t) {
    throwableReference = t;
    releaseLatch.countDown();
  }

  // ........................................................................................... //

  private synchronized void checkpointSatisfied(Checkpoint checkpoint) {
    checkpoints.remove(checkpoint);
    if (checkpoints.isEmpty()) {
      completeNow();
    }
  }

  public Checkpoint checkpoint() {
    return checkpoint(1);
  }

  public Checkpoint checkpoint(int requiredNumberOfPasses) {
    CountingCheckpoint checkpoint = new CountingCheckpoint(this::checkpointSatisfied, requiredNumberOfPasses);
    checkpoints.add(checkpoint);
    return checkpoint;
  }

  // ........................................................................................... //

  public <T> Handler<AsyncResult<T>> succeeding() {
    return ar -> {
      if (!ar.succeeded()) {
        failNow(ar.cause());
      }
    };
  }

  public <T> Handler<AsyncResult<T>> succeeding(Handler<AsyncResult<T>> nextHandler) {
    return ar -> {
      if (ar.succeeded()) {
        nextHandler.handle(ar);
      } else {
        failNow(ar.cause());
      }
    };
  }

  public <T> Handler<AsyncResult<T>> failing() {
    return ar -> {
      if (ar.succeeded()) {
        failNow(new AssertionError("The asynchronous result was expected to failNow"));
      }
    };
  }

  public <T> Handler<AsyncResult<T>> failing(Handler<AsyncResult<T>> nextHandler) {
    return ar -> {
      if (ar.succeeded()) {
        failNow(new AssertionError("The asynchronous result was expected to failNow"));
      } else {
        nextHandler.handle(ar);
      }
    };
  }

  // ........................................................................................... //

  public VertxTestContext verify(Runnable block) {
    try {
      block.run();
    } catch (Throwable t) {
      failNow(t);
    }
    return this;
  }

  // ........................................................................................... //

  public boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
    return !failed() && releaseLatch.await(timeout, unit);
  }

  // ........................................................................................... //
}
