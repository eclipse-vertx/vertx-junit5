package io.vertx.junit5.tests;

import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.jupiter.api.Assertions.*;

public class CheckpointInjectionTest {

  @VertxTest
  public static class InjectCheckpointsInSameMethod {

    private static Checkpoint checkpoint1, checkpoint2;

    @Test
    public void test(Checkpoint checkpoint1, Checkpoint checkpoint2) {
      InjectCheckpointsInSameMethod.checkpoint1 = checkpoint1;
      InjectCheckpointsInSameMethod.checkpoint2 = checkpoint2;
      doAsync(100, () -> {
        checkpoint1.flag();
        checkpoint2.flag();
      });
    }
  }

  @Test
  void testInjectCheckpointsInSameMethod() {
    try {
      TestExecutionSummary summary = VertxParameterProviderLifeCycleTest.runTests(InjectCheckpointsInSameMethod.class);
      assertEquals(1, summary.getTestsSucceededCount());
      assertNotNull(InjectCheckpointsInSameMethod.checkpoint1);
      assertNotNull(InjectCheckpointsInSameMethod.checkpoint2);
      assertNotSame(InjectCheckpointsInSameMethod.checkpoint1, InjectCheckpointsInSameMethod.checkpoint2);
    } finally {
      InjectCheckpointsInSameMethod.checkpoint1 = null;
      InjectCheckpointsInSameMethod.checkpoint2 = null;
    }
  }

  @Test
  void testInjectDifferentCheckpointsInLifecycle() {
    try {
      TestExecutionSummary summary = VertxParameterProviderLifeCycleTest.runTests(InjectDifferentCheckpointsInLifecycle.class);
      assertEquals(1, summary.getTestsSucceededCount());
      assertNotNull(InjectDifferentCheckpointsInLifecycle.checkpoint1);
      assertNotNull(InjectDifferentCheckpointsInLifecycle.checkpoint2);
      assertNotSame(InjectDifferentCheckpointsInLifecycle.checkpoint1, InjectCheckpointsInSameMethod.checkpoint2);
    } finally {
      InjectDifferentCheckpointsInLifecycle.checkpoint1 = null;
      InjectDifferentCheckpointsInLifecycle.checkpoint2 = null;
    }
  }

  @VertxTest
  public static class InjectDifferentCheckpointsInLifecycle {

    private static Checkpoint checkpoint1, checkpoint2;

    @BeforeEach
    public void before(Checkpoint checkpoint1) {
      InjectDifferentCheckpointsInLifecycle.checkpoint1 = checkpoint1;
      doAsync(100, () -> {
        checkpoint1.flag();
      });
    }

    @Test
    public void test(Checkpoint checkpoint2) {
      InjectDifferentCheckpointsInLifecycle.checkpoint2 = checkpoint2;
      doAsync(100, () -> {
        checkpoint2.flag();
      });
    }
  }

  private static void doAsync(long delay, Runnable runnable) {
    new Thread(() -> {
      try {
        Thread.sleep(delay);
      } catch (InterruptedException ignore) {
      }
      runnable.run();
    }).start();
  }

}
