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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
@DisplayName("Tests of VertxExtension")
class VertxExtensionTest {

  @Nested
  @ExtendWith(VertxExtension.class)
  @DisplayName("Basic test-level parameter injection smoke tests")
  class Injection {

    @Test
    @DisplayName("Inject a Vertx instance")
    void gimme_vertx(Vertx vertx) {
      assertNotNull(vertx);
    }

    @Test
    @DisplayName("Inject a VertxTestContext instance")
    void gimme_vertx_test_context(VertxTestContext context) {
      assertNotNull(context);
      context.completeNow();
    }

    @Test
    @DisplayName("Inject Vertx and VertxTestContext instances")
    void gimme_everything(Vertx vertx, VertxTestContext context) {
      assertNotNull(vertx);
      assertNotNull(context);
      context.completeNow();
    }
  }

  @Nested
  @ExtendWith(VertxExtension.class)
  @Timeout(4500)
  @DisplayName("Specify timeouts")
  class SpecifyTimeout {

    @Test
    @DisplayName("Override a class-level timeout")
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void a(VertxTestContext context) throws InterruptedException {
      Thread.sleep(50);
      context.completeNow();
    }

    @Test
    @DisplayName("Use the class-level timeout")
    void b(VertxTestContext context) throws InterruptedException {
      Thread.sleep(50);
      context.completeNow();
    }
  }

  @Nested
  @DisplayName("Tests that require embedding a JUnit launcher")
  class EmbeddedWithARunner {

    @Test
    @DisplayName("⚙️ Check a test failure")
    void checkFailureTest() {
      LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
        .selectors(selectClass(FailureTest.class))
        .build();
      Launcher launcher = LauncherFactory.create();
      SummaryGeneratingListener listener = new SummaryGeneratingListener();
      launcher.registerTestExecutionListeners(listener);
      launcher.execute(request);
      TestExecutionSummary summary = listener.getSummary();
      assertThat(summary.getTestsStartedCount()).isEqualTo(1);
      assertThat(summary.getTestsFailedCount()).isEqualTo(1);
      assertThat(summary.getFailures().get(0).getException()).isInstanceOf(AssertionError.class);
    }

    @Nested
    @ExtendWith(VertxExtension.class)
    @DisplayName("🚫")
    class FailureTest {

      @Test
      @Tag("programmatic")
      void thisMustFail(Vertx vertx, VertxTestContext testContext) {
        testContext.verify(() -> {
          assertTrue(false);
        });
      }
    }

    @Test
    @DisplayName("⚙️ Check a failure in the test method body rather than in a callback")
    void checkDirectFailure() {
      LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
        .selectors(selectClass(DirectFailureTest.class))
        .build();
      Launcher launcher = LauncherFactory.create();
      SummaryGeneratingListener listener = new SummaryGeneratingListener();
      launcher.registerTestExecutionListeners(listener);
      launcher.execute(request);
      TestExecutionSummary summary = listener.getSummary();
      assertThat(summary.getTestsStartedCount()).isEqualTo(1);
      assertThat(summary.getTestsFailedCount()).isEqualTo(1);
      assertThat(summary.getFailures().get(0).getException()).isInstanceOf(RuntimeException.class);
    }

    @Nested
    @ExtendWith(VertxExtension.class)
    @DisplayName("🚫")
    class DirectFailureTest {

      @Test
      @Tag("programmatic")
      @Timeout(value = 1, timeUnit = TimeUnit.SECONDS)
      void thisMustFail(VertxTestContext testContext) {
        throw new RuntimeException("YOLO");
      }
    }

    @Test
    @DisplayName("⚙️ Check a test failure with an intermediate async result verifier")
    void checkFailureTestWithIntermediateAsyncVerifier() {
      LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
        .selectors(selectClass(FailureWithIntermediateAsyncVerifierTest.class))
        .build();
      Launcher launcher = LauncherFactory.create();
      SummaryGeneratingListener listener = new SummaryGeneratingListener();
      launcher.registerTestExecutionListeners(listener);
      launcher.execute(request);
      TestExecutionSummary summary = listener.getSummary();
      assertThat(summary.getTestsStartedCount()).isEqualTo(1);
      assertThat(summary.getTestsFailedCount()).isEqualTo(1);
      assertThat(summary.getFailures().get(0).getException()).isInstanceOf(AssertionError.class);
    }

    @Nested
    @ExtendWith(VertxExtension.class)
    @DisplayName("🚫")
    class FailureWithIntermediateAsyncVerifierTest {

      @Test
      @Tag("programmatic")
      void thisMustAlsoFail(Vertx vertx, VertxTestContext testContext) {
        vertx.executeBlocking(f -> {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          f.complete(69);
        }, testContext.succeeding(i -> testContext.verify(() -> assertEquals(58, i))));
      }
    }

    @Nested
    @ExtendWith(VertxExtension.class)
    @DisplayName("🚫")
    class TimingOut {

      @Test
      @Tag("programmatic")
      @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
      void doNothing(VertxTestContext testContext) {
        testContext.checkpoint();
      }
    }

    @Test
    @DisplayName("⚙️ Check a timeout diagnosis")
    void checkTimeoutFailureTestWithIntermediateAsyncVerifier() {
      LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
        .selectors(selectClass(EmbeddedWithARunner.TimingOut.class))
        .build();
      Launcher launcher = LauncherFactory.create();
      SummaryGeneratingListener listener = new SummaryGeneratingListener();
      launcher.registerTestExecutionListeners(listener);
      launcher.execute(request);
      TestExecutionSummary summary = listener.getSummary();
      assertThat(summary.getTestsStartedCount()).isEqualTo(1);
      assertThat(summary.getTestsFailedCount()).isEqualTo(1);
      Throwable exception = summary.getFailures().get(0).getException();
      assertThat(exception)
        .isInstanceOf(TimeoutException.class)
        .hasMessageContaining("checkpoint in file VertxExtensionTest.java");
    }
  }

  private static class UselessVerticle extends AbstractVerticle {
  }

  @Nested
  @ExtendWith(VertxExtension.class)
  @DisplayName("Test parameter injection at various (non-static) levels")
  class VertxInjectionTest {

    Vertx currentVertx;
    VertxTestContext previousTestContext;

    @BeforeEach
    void prepare(Vertx vertx, VertxTestContext testContext) {
      assertThat(testContext).isNotSameAs(previousTestContext);
      previousTestContext = testContext;
      assertThat(currentVertx).isNotSameAs(vertx);
      currentVertx = vertx;
      vertx.deployVerticle(new UselessVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    @AfterEach
    void cleanup(Vertx vertx, VertxTestContext testContext) {
      assertThat(testContext).isNotSameAs(previousTestContext);
      previousTestContext = testContext;
      assertThat(vertx.deploymentIDs()).isNotEmpty().hasSize(1);
      vertx.close(testContext.succeeding(v -> testContext.completeNow()));
    }

    @RepeatedTest(10)
    @DisplayName("Test the validity of references and scoping")
    void checkDeployments(Vertx vertx, VertxTestContext testContext) {
      assertThat(testContext).isNotSameAs(previousTestContext);
      previousTestContext = testContext;
      assertThat(vertx).isSameAs(currentVertx);
      assertThat(vertx.deploymentIDs()).isNotEmpty().hasSize(1);
      testContext.completeNow();
    }

    @Nested
    @DisplayName("A nested test")
    class NestedTest {

      @RepeatedTest(10)
      @DisplayName("Test the validity of references and scoping")
      void checkDeployments(Vertx vertx, VertxTestContext testContext) {
        assertThat(testContext).isNotSameAs(previousTestContext);
        previousTestContext = testContext;
        assertThat(vertx).isSameAs(currentVertx);
        assertThat(vertx.deploymentIDs()).isNotEmpty().hasSize(1);
        testContext.completeNow();
      }
    }
  }
}
