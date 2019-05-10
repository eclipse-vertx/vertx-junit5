package io.vertx.junit5;

import io.vertx.core.Vertx;

public class OtherVertx {

  public final Vertx vertx;

  public OtherVertx(Vertx vertx) {
    this.vertx = vertx;
  }
}
