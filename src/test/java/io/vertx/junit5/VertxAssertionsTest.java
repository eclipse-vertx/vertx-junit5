package io.vertx.junit5;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** VertxAssertionsTest */
@ExtendWith(VertxExtension.class)
public class VertxAssertionsTest {

  @Test
  public void testAssertEqualsReference(Vertx vertx, VertxTestContext testContext) {
    VertxAssertions.assertEquals(testContext, null, null);
    VertxAssertions.assertEquals(testContext, "hello", "hello");
    VertxAssertions.assertEquals(testContext, Integer.valueOf(0), Integer.valueOf(0));

    testContext.completeNow();
  }

  @Test
  public void testAssertEqualsPrimitive(Vertx vertx, VertxTestContext testContext) {

    VertxAssertions.assertEquals(testContext, (byte) 1, (byte) 1);
    VertxAssertions.assertEquals(testContext, 2, 2);
    VertxAssertions.assertEquals(testContext, 3L, 3L);
    VertxAssertions.assertEquals(testContext, (short) 4, (short) 4);
    VertxAssertions.assertEquals(testContext, (float) 5.0, (float) 5.0);
    VertxAssertions.assertEquals(testContext, (double) 6.0, (double) 6.0);

    VertxAssertions.assertEquals(testContext, true, true);
    VertxAssertions.assertEquals(testContext, false, false);

    VertxAssertions.assertEquals(testContext, 'a', 'a');
    VertxAssertions.assertEquals(testContext, '\u0000', '\u0000');

    testContext.completeNow();
  }

  @Test
  public void testAssertNotEqualsReference(Vertx vertx, VertxTestContext testContext) {
    VertxAssertions.assertNotEquals(testContext, "hello", "world");
    VertxAssertions.assertNotEquals(testContext, Integer.valueOf(0), Integer.valueOf(1));

    testContext.completeNow();
  }

  @Test
  public void testAssertNotEqualsPrimitive(Vertx vertx, VertxTestContext testContext) {

    VertxAssertions.assertNotEquals(testContext, (byte) 1, (byte) 2);
    VertxAssertions.assertNotEquals(testContext, 2, 3);
    VertxAssertions.assertNotEquals(testContext, 3L, 4L);
    VertxAssertions.assertNotEquals(testContext, (short) 4, (short) 5);
    VertxAssertions.assertNotEquals(testContext, (float) 5.0, (float) 6.0);
    VertxAssertions.assertNotEquals(testContext, (double) 6.0, (double) 7.0);

    VertxAssertions.assertNotEquals(testContext, true, false);
    VertxAssertions.assertNotEquals(testContext, false, true);

    VertxAssertions.assertNotEquals(testContext, 'a', 'b');
    VertxAssertions.assertNotEquals(testContext, '\u0000', '\uffff');

    testContext.completeNow();
  }
}
