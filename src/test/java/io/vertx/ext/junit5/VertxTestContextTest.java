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

import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
class VertxTestContextTest {

  @Test
  void fail_with_null() {
    VertxTestContext context = new VertxTestContext();
    assertThatThrownBy(() -> context.failNow(null))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("The exception cannot be null");
  }

  @Test
  void check_async_assert() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.succeeding().handle(Future.succeededFuture());
    context.awaitCompletion(1, TimeUnit.MILLISECONDS);
    assertThat(context.completed()).isTrue();

    context = new VertxTestContext();
    context.succeeding().handle(Future.failedFuture(new RuntimeException("Plop")));
    context.awaitCompletion(1, TimeUnit.MILLISECONDS);
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure())
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Plop");
  }

  @Test
  void check_async_assert_fail() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.failing().handle(Future.failedFuture("Bam"));
    context.awaitCompletion(1, TimeUnit.MILLISECONDS);
    assertThat(context.failed()).isFalse();

    context = new VertxTestContext();
    context.failing().handle(Future.succeededFuture());
    context.awaitCompletion(1, TimeUnit.MILLISECONDS);
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure()).hasMessage("The asynchronous result was expected to failNow");
  }

  @Test
  void check_async_assert_with_handler() throws InterruptedException {
    AtomicBoolean checker = new AtomicBoolean(false);
    VertxTestContext context = new VertxTestContext();

    VertxTestContext finalContext = context;
    Handler<Object> nextHandler = obj -> {
      checker.set(true);
      finalContext.completeNow();
    };

    context.succeeding(nextHandler).handle(Future.succeededFuture());
    assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
    assertThat(context.completed()).isTrue();
    assertThat(checker).isTrue();

    checker.set(false);
    context = new VertxTestContext();

    context.succeeding(nextHandler).handle(Future.failedFuture(new RuntimeException("Plop")));
    assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isFalse();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure())
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Plop");
    assertThat(checker).isFalse();
  }

  @Test
  void check_async_assert_fail_with_handler() throws InterruptedException {
    AtomicBoolean checker = new AtomicBoolean(false);
    VertxTestContext context = new VertxTestContext();

    VertxTestContext finalContext = context;
    Handler<Throwable> nextHandler = ar -> {
      checker.set(true);
      finalContext.completeNow();
    };

    context.failing(nextHandler).handle(Future.failedFuture("Bam"));
    assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
    assertThat(context.completed()).isTrue();
    assertThat(checker).isTrue();

    checker.set(false);
    context = new VertxTestContext();

    context.failing(nextHandler).handle(Future.succeededFuture());
    assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isFalse();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure()).hasMessage("The asynchronous result was expected to failNow");
  }

  @Test
  void check_verify_ok() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.verify(() -> {
      assertThat("ok").isEqualTo("ok");
      context.completeNow();
    });
    assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isTrue();
  }

  @Test
  void check_verify_fail() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.verify(() -> {
      throw new RuntimeException("Bam");
    });
    assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isFalse();

    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure())
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Bam");
  }

  @Test
  void check_checkpoint() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();

    Checkpoint a = context.checkpoint();
    Checkpoint b = context.checkpoint();

    new Thread(a::flag).start();
    new Thread(b::flag).start();
    assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isTrue();
  }

  @Test
  void checK_not_all_checkpoints_passed_timesout() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();

    Checkpoint a = context.checkpoint(2);
    context.checkpoint();

    new Thread(a::flag).start();
    new Thread(a::flag).start();
    assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isFalse();
  }

  @Test
  void complete_then_fail() {
    VertxTestContext context = new VertxTestContext();

    context.completeNow();
    context.failNow(new IllegalStateException("Oh"));

    assertThat(context.completed()).isTrue();
    assertThat(context.failed()).isFalse();
    assertThat(context.causeOfFailure()).isNull();
  }
}
