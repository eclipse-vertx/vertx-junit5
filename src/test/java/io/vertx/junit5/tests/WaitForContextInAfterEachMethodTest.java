package io.vertx.junit5.tests;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicBoolean;

@Disabled("Executed only via programmatic launcher from RunAfterEachContextCheckTest")
@ExtendWith({VertxExtension.class})
public class WaitForContextInAfterEachMethodTest {
  static final AtomicBoolean afterTestContextAwaited = new AtomicBoolean(false);

  @Test
  void test(final VertxTestContext context) {
    context.failNow(new RuntimeException("Failing test through context"));
  }

  @AfterAll
  static void afterAll(final VertxTestContext context) {
    if (afterTestContextAwaited.get()) {
      context.completeNow();
    } else {
      context.failNow(new RuntimeException("afterTest context was not awaited"));
    }
  }

  @AfterEach
  void afterTest(final Vertx vertx, final VertxTestContext context) {
    vertx.setTimer(100, id -> {
      afterTestContextAwaited.set(true);
      context.completeNow();
    });
  }

}
