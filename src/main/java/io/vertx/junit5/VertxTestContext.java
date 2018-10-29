/*
 * Copyright (c) 2018 Red Hat, Inc.
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

package io.vertx.junit5;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A test context to wait on the outcomes of asynchronous operations.
 *
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public final class VertxTestContext {

  private Throwable throwableReference = null;
  private final CountDownLatch releaseLatch = new CountDownLatch(1);
  private final HashSet<Checkpoint> checkpoints = new HashSet<>();

  // ........................................................................................... //

  /**
   * Check if the context has been marked has failed or not.
   *
   * @return {@code true} if the context has failed, {@code false} otherwise.
   */
  public synchronized boolean failed() {
    return throwableReference != null;
  }

  /**
   * Give the cause of failure.
   *
   * @return the cause of failure, or {@code null} if the test context hasn't failed.
   */
  public synchronized Throwable causeOfFailure() {
    return throwableReference;
  }

  /**
   * Check if the context has completed.
   *
   * @return {@code true} if the context has completed, {@code false} otherwise.
   */
  public synchronized boolean completed() {
    return !failed() && releaseLatch.getCount() == 0;
  }

  // ........................................................................................... //

  /**
   * Complete the test context immediately, making the corresponding test pass.
   */
  public synchronized void completeNow() {
    releaseLatch.countDown();
  }

  /**
   * Make the test context fail immediately, making the corresponding test fail.
   *
   * @param t the cause of failure.
   */
  public synchronized void failNow(Throwable t) {
    Objects.requireNonNull(t, "The exception cannot be null");
    if (!completed()) {
      throwableReference = t;
      releaseLatch.countDown();
    }
  }

  // ........................................................................................... //

  private synchronized void checkpointSatisfied(Checkpoint checkpoint) {
    checkpoints.remove(checkpoint);
    if (checkpoints.isEmpty()) {
      completeNow();
    }
  }

  /**
   * Create a lax checkpoint.
   *
   * @return a checkpoint that requires 1 pass; more passes are allowed and ignored.
   */
  public Checkpoint laxCheckpoint() {
    return laxCheckpoint(1);
  }

  /**
   * Create a lax checkpoint.
   *
   * @param requiredNumberOfPasses the required number of passes to validate the checkpoint.
   * @return a checkpoint that requires several passes; more passes than the required number are allowed and ignored.
   */
  public synchronized Checkpoint laxCheckpoint(int requiredNumberOfPasses) {
    CountingCheckpoint checkpoint = CountingCheckpoint.laxCountingCheckpoint(this::checkpointSatisfied, requiredNumberOfPasses);
    checkpoints.add(checkpoint);
    return checkpoint;
  }

  /**
   * Create a strict checkpoint.
   *
   * @return a checkpoint that requires 1 pass, and makes the context fail if it is called more than once.
   */
  public Checkpoint checkpoint() {
    return checkpoint(1);
  }

  /**
   * Create a strict checkpoint.
   *
   * @param requiredNumberOfPasses the required number of passes to validate the checkpoint.
   * @return a checkpoint that requires several passes, but no more or it fails the context.
   */
  public synchronized Checkpoint checkpoint(int requiredNumberOfPasses) {
    CountingCheckpoint checkpoint = CountingCheckpoint.strictCountingCheckpoint(this::checkpointSatisfied, this::failNow, requiredNumberOfPasses);
    checkpoints.add(checkpoint);
    return checkpoint;
  }

  // ........................................................................................... //

  /**
   * Create an asynchronous result handler that expects a success.
   *
   * @param <T> the asynchronous result type.
   * @return the handler.
   */
  public <T> Handler<AsyncResult<T>> succeeding() {
    return ar -> {
      if (!ar.succeeded()) {
        failNow(ar.cause());
      }
    };
  }

  /**
   * Create an asynchronous result handler that expects a success, and passes the value to another handler.
   *
   * @param nextHandler the value handler to call on success.
   * @param <T>         the asynchronous result type.
   * @return the handler.
   */
  public <T> Handler<AsyncResult<T>> succeeding(Handler<T> nextHandler) {
    Objects.requireNonNull(nextHandler, "The handler cannot be null");
    return ar -> {
      if (ar.succeeded()) {
        nextHandler.handle(ar.result());
      } else {
        failNow(ar.cause());
      }
    };
  }

  /**
   * Create an asynchronous result handler that expects a failure.
   *
   * @param <T> the asynchronous result type.
   * @return the handler.
   */
  public <T> Handler<AsyncResult<T>> failing() {
    return ar -> {
      if (ar.succeeded()) {
        failNow(new AssertionError("The asynchronous result was expected to have failed"));
      }
    };
  }

  /**
   * Create an asynchronous result handler that expects a failure, and passes the exception to another handler.
   *
   * @param nextHandler the exception handler to call on failure.
   * @param <T>         the asynchronous result type.
   * @return the handler.
   */
  public <T> Handler<AsyncResult<T>> failing(Handler<Throwable> nextHandler) {
    Objects.requireNonNull(nextHandler, "The handler cannot be null");
    return ar -> {
      if (ar.succeeded()) {
        failNow(new AssertionError("The asynchronous result was expected to have failed"));
      } else {
        nextHandler.handle(ar.cause());
      }
    };
  }

  /**
   * Create an asynchronous result handler that expects a success to then complete the test context.
   *
   * @param <T> the asynchronous result type.
   * @return the handler.
   */
  public <T> Handler<AsyncResult<T>> completing() {
    return ar -> {
      if (ar.succeeded()) {
        completeNow();
      } else {
        failNow(ar.cause());
      }
    };
  }

  // ........................................................................................... //

  /**
   * This method allows you to check if a future is completed. It internally creates a checkpoint. You can use it in a future chain
   *
   * @param fut The future to assert success
   * @return a future with completion result
   */
  public <T> Future<T> assertComplete(Future<T> fut) {
    Future<T> newFut = Future.future();
    fut.setHandler(ar -> {
      if (ar.succeeded()) {
        newFut.complete(ar.result());
      } else {
        Throwable ex = new AssertionError("Future failed with exception: " + ar.cause().getMessage(), ar.cause());
        this.failNow(ex);
        newFut.fail(ex);
      }
    });
    return newFut;
  }

  /**
   * This method allows you to check if a future is failed. It internally creates a checkpoint. You can use it in a future chain
   *
   * @param fut The future to assert failure
   * @return a future with failure result
   */
  public <T> Future<T> assertFailure(Future<T> fut) {
    Future<T> newFut = Future.future();
    fut.setHandler(ar -> {
      if (ar.succeeded()) {
        Throwable ex = new AssertionError("Future is completed with value: " + ar.result());
        this.failNow(ex);
        newFut.fail(ex);
      } else {
        newFut.fail(ar.cause());
      }
    });
    return newFut;
  }

  // ........................................................................................... //

  /**
   * Allow verifications and assertions to be made.
   * <p>
   * This method allows any assertion API to be used.
   * The semantic is that the verification is successful when no exception is being thrown upon calling {@code block},
   * otherwise the context fails with that exception.
   *
   * @param block a block of code to execute.
   * @return this context.
   */
  public VertxTestContext verify(Runnable block) {
    Objects.requireNonNull(block, "The block cannot be null");
    try {
      block.run();
    } catch (Throwable t) {
      failNow(t);
    }
    return this;
  }

  // ........................................................................................... //

  /**
   * Wait for the completion of the test context.
   * <p>
   * This method is automatically called by the {@link VertxExtension} when using parameter injection of {@link VertxTestContext}.
   * You should only call it when you instantiate this class manually.
   *
   * @param timeout the timeout.
   * @param unit    the timeout unit.
   * @return {@code true} if the completion or failure happens before the timeout has been reached, {@code false} otherwise.
   * @throws InterruptedException when the thread has been interrupted.
   */
  public boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
    return failed() || releaseLatch.await(timeout, unit);
  }

  // ........................................................................................... //
}
