package io.vertx.junit5;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(VertxExtension.class)
@ExtendWith(OtherExtension.class)
public class OtherExtensionIntegrationTest {

  @Test
  public void testParam(Vertx vertx, OtherVertx otherVertx) {
    assertEquals(vertx, otherVertx.vertx);
  }

  @Test
  public void testInvertedParam(OtherVertx otherVertx, Vertx vertx) {
    assertEquals(vertx, otherVertx.vertx);
  }

  @Test
  public void testOnlyOtherVertx(OtherVertx otherVertx) {
    assertNotNull(otherVertx.vertx);
  }
}
