package io.vertx.junit5;

import org.junit.jupiter.api.Assertions;

/** VertxAssertions */
public interface VertxAssertions {

  public static <T> void assertEquals(VertxTestContext testContext, T expected, T actual) {
    try {
      Assertions.assertEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertEquals(VertxTestContext testContext, byte expected, byte actual) {
    try {
      Assertions.assertEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertEquals(VertxTestContext testContext, short expected, short actual) {
    try {
      Assertions.assertEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertEquals(VertxTestContext testContext, int expected, int actual) {
    try {
      Assertions.assertEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertEquals(VertxTestContext testContext, long expected, long actual) {
    try {
      Assertions.assertEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertEquals(VertxTestContext testContext, float expected, float actual) {
    try {
      Assertions.assertEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertEquals(VertxTestContext testContext, double expected, double actual) {
    try {
      Assertions.assertEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertEquals(VertxTestContext testContext, boolean expected, boolean actual) {
    try {
      Assertions.assertEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertEquals(VertxTestContext testContext, char expected, char actual) {
    try {
      Assertions.assertEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static <T> void assertNotEquals(VertxTestContext testContext, T expected, T actual) {
    try {
      Assertions.assertNotEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertNotEquals(VertxTestContext testContext, byte expected, byte actual) {
    try {
      Assertions.assertNotEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertNotEquals(VertxTestContext testContext, short expected, short actual) {
    try {
      Assertions.assertNotEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertNotEquals(VertxTestContext testContext, int expected, int actual) {
    try {
      Assertions.assertNotEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertNotEquals(VertxTestContext testContext, long expected, long actual) {
    try {
      Assertions.assertNotEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertNotEquals(VertxTestContext testContext, float expected, float actual) {
    try {
      Assertions.assertNotEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertNotEquals(VertxTestContext testContext, double expected, double actual) {
    try {
      Assertions.assertNotEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertNotEquals(
      VertxTestContext testContext, boolean expected, boolean actual) {
    try {
      Assertions.assertNotEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }

  public static void assertNotEquals(VertxTestContext testContext, char expected, char actual) {
    try {
      Assertions.assertNotEquals(expected, actual);
    } catch (Throwable t) {
      testContext.failNow(t);
    }
  }
}
