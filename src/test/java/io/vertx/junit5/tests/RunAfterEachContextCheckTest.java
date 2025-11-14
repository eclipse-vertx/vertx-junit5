package io.vertx.junit5.tests;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunAfterEachContextCheckTest {

  @Test
  void runsWaitForContextInAfterEachMethodTestAndChecksAfterAllSucceeded() {
    // Select only the target class
    final DiscoverySelector selector = DiscoverySelectors.selectClass(WaitForContextInAfterEachMethodTest.class);

    // Collect a summary for assertions
    final SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();

    // Capture the class container result specifically (to assert @AfterAll behavior)
    final AtomicReference<TestExecutionResult.Status> classStatus = new AtomicReference<>();

    final TestExecutionListener captureClassStatus = new TestExecutionListener() {
      @Override
      public void executionFinished(final TestIdentifier id, final TestExecutionResult result) {
        id.getSource()
          .ifPresent(source -> {
            if (id.isContainer() && source instanceof ClassSource) {
              final ClassSource cs = (ClassSource) source;
              if (cs.getClassName().equals(WaitForContextInAfterEachMethodTest.class.getName())) {
                classStatus.set(result.getStatus());
              }
            }
          });
      }
    };

    final LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
      .selectors(selector)
      .filters(EngineFilter.includeEngines("junit-jupiter"))
      // Make @Disabled inert for this run
      .configurationParameter(
        "junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
      // Make execution deterministic
      .configurationParameter("junit.jupiter.execution.parallel.enabled", "false")
      .build();

    final Launcher launcher = LauncherFactory.create();
    launcher.registerTestExecutionListeners(summaryListener, captureClassStatus);

    launcher.execute(request);

    final TestExecutionSummary summary = summaryListener.getSummary();

    // The single test method intentionally fails
    assertEquals(1, summary.getTestsFoundCount(), "Expect exactly one test discovered");
    assertEquals(1, summary.getTestsFailedCount(), "The test should fail via context.failNow()");
    assertEquals(0, summary.getContainersFailedCount(), "The test class container should not fail");

    // Critically: the class container (where @AfterAll runs) must be SUCCESSFUL
    assertEquals(TestExecutionResult.Status.SUCCESSFUL,
      classStatus.get(),
      "@AfterAll must complete its VertxTestContext without failure");
  }
}
