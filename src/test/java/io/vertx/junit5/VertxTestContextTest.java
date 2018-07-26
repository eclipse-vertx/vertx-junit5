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

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
@DisplayName("Unit tests for VertxTestContext")
class VertxTestContextTest {

  @Test
  @DisplayName("Check that failing with a null exception is forbidden")
  void fail_with_null() {
    VertxTestContext context = new VertxTestContext();
    assertThatThrownBy(() -> context.failNow(null))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("The exception cannot be null");
  }

  @Test
  @DisplayName("Check the behavior of succeeding() and that it does not complete the test context")
  void check_async_assert() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.succeeding().handle(Future.succeededFuture());
    assertThat(context.awaitCompletion(1, TimeUnit.MILLISECONDS)).isFalse();
    assertThat(context.completed()).isFalse();

    context = new VertxTestContext();
    context.succeeding().handle(Future.failedFuture(new RuntimeException("Plop")));
    assertThat(context.awaitCompletion(1, TimeUnit.MILLISECONDS)).isTrue();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure())
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Plop");
  }

  @Test
  @DisplayName("Check the behavior of failing()")
  void check_async_assert_fail() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.failing().handle(Future.failedFuture("Bam"));
    context.awaitCompletion(1, TimeUnit.MILLISECONDS);
    assertThat(context.failed()).isFalse();

    context = new VertxTestContext();
    context.failing().handle(Future.succeededFuture());
    context.awaitCompletion(1, TimeUnit.MILLISECONDS);
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure()).hasMessage("The asynchronous result was expected to have failed");
  }

  @Test
  @DisplayName("Check the behavior of succeeding(callback)")
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
    assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure())
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Plop");
    assertThat(checker).isFalse();
  }

  @Test
  @DisplayName("Check the behavior of failing(callback)")
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
    assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure()).hasMessage("The asynchronous result was expected to have failed");
  }

  @Test
  @DisplayName("Check the behavior of verify() and no error")
  void check_verify_ok() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.verify(() -> {
      assertThat("ok").isEqualTo("ok");
      context.completeNow();
    });
    assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isTrue();
  }

  @Test
  @DisplayName("Check the behavior of verify() with an error")
  void check_verify_fail() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.verify(() -> {
      throw new RuntimeException("Bam");
    });
    assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isTrue();

    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure())
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Bam");
  }

  @Test
  @DisplayName("Check that flagging 2 checkpoints completes the test context")
  void check_checkpoint() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();

    Checkpoint a = context.checkpoint();
    Checkpoint b = context.checkpoint();

    new Thread(a::flag).start();
    new Thread(b::flag).start();
    assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isTrue();
  }

  @Test
  @DisplayName("Check that not flagging all checkpoints ends up in a timeout")
  void checK_not_all_checkpoints_passed_timesout() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();

    Checkpoint a = context.checkpoint(2);
    context.checkpoint();

    new Thread(a::flag).start();
    new Thread(a::flag).start();
    assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isFalse();
  }

  @Test
  @DisplayName("Check that flagging strict checkpoints more than expected fails the test context")
  void check_strict_checkpoint_overuse() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();

    Checkpoint a = context.strictCheckpoint();
    Checkpoint b = context.checkpoint();
    new Thread(a::flag).start();
    new Thread(a::flag).start();

    assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isTrue();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("flagged too many times");
  }

  @Test
  @DisplayName("Check that failing an already completed context is not possible")
  void complete_then_fail() {
    VertxTestContext context = new VertxTestContext();

    context.completeNow();
    context.failNow(new IllegalStateException("Oh"));

    assertThat(context.completed()).isTrue();
    assertThat(context.failed()).isFalse();
    assertThat(context.causeOfFailure()).isNull();
  }

  @Test
  @DisplayName("Just fail immediately and on the test runner thread")
  void just_fail() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.failNow(new RuntimeException("Woops"));
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.failed()).isTrue();
  }

  @Test
  @DisplayName("Pass a success to a completing() async handler")
  void check_completing_success() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.completing().handle(Future.succeededFuture());
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.completed()).isTrue();
  }

  @Test
  @DisplayName("Pass a failure to a completing() async handler")
  void check_completing_failure() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.completing().handle(Future.failedFuture(new RuntimeException("Boo!")));
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.completed()).isFalse();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure()).hasMessage("Boo!");
  }
}
