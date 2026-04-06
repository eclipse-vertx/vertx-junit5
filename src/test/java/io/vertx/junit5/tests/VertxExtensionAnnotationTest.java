package io.vertx.junit5.tests;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTest;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.vertx.junit5.tests.VertxParameterProviderLifeCycleTest.FAILURE;
import static org.junit.jupiter.api.Assertions.*;

public class VertxExtensionAnnotationTest {

  @VertxTest(instrumentVertx = false)
  public static class AnnotatedTest {

    static Vertx vertx;

    @Test
    public void test(Vertx vertx) {
      AnnotatedTest.vertx = vertx;
    }
  }

  @Test
  void testDeclaration() {
    try {
      TestExecutionSummary summary = VertxParameterProviderLifeCycleTest.runTests(AnnotatedTest.class);
      assertEquals(1, summary.getTestsSucceededCount());
      assertNotNull(AnnotatedTest.vertx);
    } finally {
      AnnotatedTest.vertx = null;
    }
  }

  @VertxTest
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
    TestExecutionSummary summary = VertxParameterProviderLifeCycleTest.runTests(TestFailureWithContext.class);
    assertEquals(1, summary.getFailures().size());
    assertSame(FAILURE, summary.getFailures().get(0).getException().getCause());
  }

  @VertxTest
  public static class TestHolder {

    @Nested
    public class ActualTest {

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
  }

  @Test
  public void testNestedTest() {
    TestExecutionSummary summary = VertxParameterProviderLifeCycleTest.runTests(TestHolder.class);
    assertEquals(1, summary.getFailures().size());
    summary.getFailures().get(0).getException().printStackTrace(System.out);
    assertSame(FAILURE, summary.getFailures().get(0).getException().getCause());
  }

  @VertxTest
  public static abstract class ParentTest {
  }

  public static class ChildTest extends ParentTest {

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
  public void testConfigInheritance() {
    TestExecutionSummary summary = VertxParameterProviderLifeCycleTest.runTests(ChildTest.class);
    assertEquals(1, summary.getFailures().size());
    summary.getFailures().get(0).getException().printStackTrace(System.out);
    assertSame(FAILURE, summary.getFailures().get(0).getException().getCause());
  }

  @VertxTest
  public static class VertxCreatedInBeforeEachTest {

    private Vertx vertx;

    @BeforeEach
    void setUp(Vertx vertx) {
      this.vertx = vertx;
    }

    @Test
    public void test(VertxTestContext ctx) throws Exception {
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
  public void testVertxCreatedInBeforeEachTest() {
    TestExecutionSummary summary = VertxParameterProviderLifeCycleTest.runTests(VertxCreatedInBeforeEachTest.class);
    assertEquals(1, summary.getFailures().size());
    summary.getFailures().get(0).getException().printStackTrace(System.out);
    assertSame(FAILURE, summary.getFailures().get(0).getException().getCause());
  }

  @VertxTest
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
    TestExecutionSummary summary = VertxParameterProviderLifeCycleTest.runTests(TestReportFailureBare.class);
    assertEquals(1, summary.getFailures().size());
    assertSame(FAILURE, summary.getFailures().get(0).getException().getCause());
  }
}
