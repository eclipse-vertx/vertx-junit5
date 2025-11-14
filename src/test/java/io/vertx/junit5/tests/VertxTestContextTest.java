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

package io.vertx.junit5.tests;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
@DisplayName("Unit tests for VertxTestContext")
class VertxTestContextTest {

  @Test
  @DisplayName("Check that failing with a null exception is forbidden")
  void fail_with_null() {
    VertxTestContext context = new VertxTestContext();
    assertThatThrownBy(() -> context.failNow((Throwable) null))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("The exception cannot be null");
  }

  @SuppressWarnings("deprecation")
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
  @DisplayName("Check callback exception of succeeding(callback)")
  void check_succeeding_callback_with_exception() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    Handler<Object> nextHandler = obj -> {
      throw new RuntimeException("Boom");
    };
    context.succeeding(nextHandler).handle(Future.succeededFuture());
    assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure())
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Boom");
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
  @DisplayName("Check callback exception of failing(callback)")
  void check_failing_callback_with_exception() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    Handler<Throwable> nextHandler = throwable -> {
      throw new RuntimeException("Pow");
    };
    context.failing(nextHandler).handle(Future.failedFuture("some failure"));
    assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure())
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Pow");
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

    Checkpoint a = context.checkpoint();
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
  @DisplayName("Check that a lax checkpoint can be flagged more often than required")
  void check_lax_checkpoint_no_overuse() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();

    Checkpoint a = context.laxCheckpoint();
    Checkpoint b = context.checkpoint();
    new Thread(() -> {
      a.flag();
      a.flag();
      a.flag();
      b.flag();
    }).start();

    assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isTrue();
    assertThat(context.failed()).isFalse();
    assertThat(context.completed()).isTrue();
  }

  @Test
  @DisplayName("Check that failing an already completed context is possible")
  void complete_then_fail() {
    VertxTestContext context = new VertxTestContext();

    context.completeNow();
    context.failNow(new IllegalStateException("Oh"));

    assertThat(context.completed()).isFalse();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure()).isInstanceOf(IllegalStateException.class).hasMessage("Oh");
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
  @DisplayName("Pass a success to a succeedingThenComplete() async handler")
  void check_succeedingThenComplete_success() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.succeedingThenComplete().handle(Future.succeededFuture());
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.completed()).isTrue();
  }

  @Test
  @DisplayName("Pass a failure to a succeedingThenComplete() async handler")
  void check_succeedingThenComplete_failure() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.succeedingThenComplete().handle(Future.failedFuture(new RuntimeException("Boo!")));
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.completed()).isFalse();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure()).hasMessage("Boo!");
  }

  @Test
  @DisplayName("Pass a failure to a failingThenComplete() async handler")
  void check_failingThenComplete_failure() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.failingThenComplete().handle(Future.failedFuture(new IllegalArgumentException("42")));
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.failed()).isFalse();
    assertThat(context.completed()).isTrue();
    assertThat(context.causeOfFailure()).isNull();
  }

  @Test
  @DisplayName("Pass a success to a failingThenComplete() async handler")
  void check_failingThenComplete_success() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.failingThenComplete().handle(Future.succeededFuture("gold"));
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.completed()).isFalse();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure())
      .isInstanceOf(AssertionError.class)
      .hasMessage("The asynchronous result was expected to have failed");
  }

  @Test
  @DisplayName("Pass future assertComplete")
  void check_future_completion() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context
      .assertComplete(Future.succeededFuture("bla"))
      .compose(s -> context.assertComplete(Future.succeededFuture(s + "bla")))
      .onComplete(context.succeeding(res -> {
        assertThat(res).isEqualTo("blabla");
        context.completeNow();
      }));
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.completed()).isTrue();
  }

  @Test
  @DisplayName("Fail future assertComplete")
  void check_future_completion_failure() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context
      .assertComplete(Future.succeededFuture("bla"))
      .compose(s -> context.assertComplete(Future.failedFuture(new IllegalStateException(s + "bla"))))
      .onComplete(context.succeeding(res -> {
        context.completeNow();
      }));
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.completed()).isFalse();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure())
      .isInstanceOf(AssertionError.class);
    assertThat(context.causeOfFailure().getCause())
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("blabla");
  }

  @Test
  @DisplayName("Pass future chain assertComplete")
  void check_future_chain_completion() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context
      .assertComplete(Future.succeededFuture("bla")
        .compose(s -> Future.failedFuture(new IllegalStateException(s + "bla")))
        .recover(ex -> Future.succeededFuture(ex.getMessage()))
      )
      .onComplete(context.succeeding(res -> {
        assertThat(res).isEqualTo("blabla");
        context.completeNow();
      }));
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.completed()).isTrue();
  }

  @Test
  @DisplayName("Fail future chain assertComplete")
  void check_future_chain_completion_failure() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context
      .assertComplete(Future.succeededFuture("bla")
        .compose(s -> Future.failedFuture(new IllegalStateException(s + "bla")))
      )
      .onComplete(context.succeeding(res -> {
        context.completeNow();
      }));
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.completed()).isFalse();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure())
      .isInstanceOf(AssertionError.class);
    assertThat(context.causeOfFailure().getCause())
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("blabla");
  }

  @Test
  @DisplayName("Pass future assertFailure")
  void check_future_failing() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context
      .assertFailure(Future.failedFuture(new IllegalStateException("bla")))
      .recover(s -> context.assertFailure(Future.failedFuture(new IllegalStateException(s.getMessage() + "bla"))))
      .onComplete(context.failing(ex -> {
        assertThat(ex)
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("blabla");
        context.completeNow();
      }));
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.completed()).isTrue();
  }

  @Test
  @DisplayName("Fail future assertComplete")
  void check_future_failing_failure() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context
      .assertFailure(Future.failedFuture(new IllegalStateException("bla")))
      .recover(s -> context.assertFailure(Future.succeededFuture(s.getMessage() + "bla")))
      .onComplete(context.succeeding(res -> {
        context.completeNow();
      }));
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.completed()).isFalse();
    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure())
      .isInstanceOf(AssertionError.class)
      .hasMessage("Future completed with value: blabla");
  }

  @Test
  @DisplayName("Call verify() with a block that throws an exception")
  void check_verify_with_exception() throws InterruptedException {
    VertxTestContext context = new VertxTestContext();
    context.verify(() -> {
      throw new RuntimeException("!");
    });
    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
    assertThat(context.causeOfFailure())
      .hasMessage("!")
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("Check that unsatisfied call sites are properly identified")
  void check_unsatisifed_checkpoint_callsites() {
    VertxTestContext context = new VertxTestContext();
    Checkpoint a = context.checkpoint();
    Checkpoint b = context.checkpoint(2);

    assertThat(context.unsatisfiedCheckpointCallSites()).hasSize(2);

    a.flag();
    b.flag();
    assertThat(context.unsatisfiedCheckpointCallSites()).hasSize(1);

    StackTraceElement element = context.unsatisfiedCheckpointCallSites().iterator().next();
    assertThat(element.getClassName()).isEqualTo(VertxTestContextTest.class.getName());
    assertThat(element.getMethodName()).isEqualTo("check_unsatisifed_checkpoint_callsites");

    b.flag();
    assertThat(context.unsatisfiedCheckpointCallSites()).isEmpty();
  }

  @Test
  @DisplayName("Check failNow() called with a string")
  void check_fail_now_called_with_a_string() {
    VertxTestContext context = new VertxTestContext();

    context.failNow("error message");

    assertThat(context.failed()).isTrue();
    assertThat(context.causeOfFailure()).hasMessage("error message");
  }
}
