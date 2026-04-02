package io.vertx.junit5.tests;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.junit5.ReportHandlerFailures;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class VertxParameterProviderLifeCycleTest {

  private static final AssertionError FAILURE = new AssertionError();

  @ExtendWith(VertxExtension.class)
  public static class TestVertxInstanceClosed {

    static Vertx vertx;

    @Test
    public void test(Vertx vertx) throws Exception {
      TestVertxInstanceClosed.vertx = vertx;
    }
  }

  @Test
  public void testVertxInstanceClosed() {
    TestExecutionSummary summary = runTests(TestVertxInstanceClosed.class);
    Vertx v = TestVertxInstanceClosed.vertx;
    try {
      v.runOnContext(v2 -> {
      });
      fail();
    } catch (RejectedExecutionException ignore) {
      // OK
    }
  }

  @ExtendWith(VertxExtension.class)
  @ReportHandlerFailures
  public static class TestFailureWithContext {

    @Test
    public void test(Vertx vertx, VertxTestContext ctx) throws Exception {
      CountDownLatch latch = new CountDownLatch(1);
      vertx.runOnContext(v -> {
        vertx.runOnContext(v2 -> {
          latch.countDown();
        });
        throw FAILURE;
      });
      latch.await(20, TimeUnit.SECONDS);
    }
  }

  @Test
  public void testFailureWithContext() {
    TestExecutionSummary summary = runTests(TestFailureWithContext.class);
    assertEquals(1, summary.getFailures().size());
    assertSame(FAILURE, summary.getFailures().get(0).getException().getCause());
  }

  @ExtendWith(VertxExtension.class)
  public static class TestFailureWithContextBare {

    static Handler<Throwable> exceptionHandler;

    @Test
    public void test(Vertx vertx, VertxTestContext ctx) throws Exception {
      exceptionHandler = vertx.exceptionHandler();
      ctx.completeNow();
    }
  }

  @Test
  public void testFailureWithContextBare() {
    TestExecutionSummary summary = runTests(TestFailureWithContextBare.class);
    assertEquals(0, summary.getFailures().size());
    assertNull(TestFailureWithContextBare.exceptionHandler);
  }

  @ExtendWith(VertxExtension.class)
  @ReportHandlerFailures
  public static class TestReportFailureBare {

    @Test
    public void test(Vertx vertx) throws Exception {
      CountDownLatch latch = new CountDownLatch(1);
      vertx.runOnContext(v -> {
        vertx.runOnContext(v2 -> {
          latch.countDown();
        });
        throw FAILURE;
      });
      latch.await(20, TimeUnit.SECONDS);
    }
  }

  @Disabled("Requires to use a VertxTestContext argument")
  @Test()
  public void testReportFailureBare() {
    TestExecutionSummary summary = runTests(TestReportFailureBare.class);
    assertEquals(1, summary.getFailures().size());
    assertSame(FAILURE, summary.getFailures().get(0).getException().getCause());
  }

  private TestExecutionSummary runTests(Class<?> clazz) {

    LauncherDiscoveryRequest discoveryRequest = LauncherDiscoveryRequestBuilder.request()
      .selectors(selectClass(clazz))
      .build();

    SummaryGeneratingListener listener = new SummaryGeneratingListener();

    try (LauncherSession session = LauncherFactory.openSession()) {
      Launcher launcher = session.getLauncher();
      // Register one ore more listeners of your choice.
      launcher.registerTestExecutionListeners(listener);
      // Discover tests and build a test plan.
      TestPlan testPlan = launcher.discover(discoveryRequest);
      // Execute the test plan.
      launcher.execute(testPlan);
      // Alternatively, execute the discovery request directly.
      launcher.execute(discoveryRequest);
    }

    return listener.getSummary();
  }


}
