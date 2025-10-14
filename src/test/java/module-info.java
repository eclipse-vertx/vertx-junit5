@SuppressWarnings("module")
open module io.vertx.testing.junit5.tests {
  requires io.vertx.core;
  requires io.vertx.testing.junit5;
  requires org.junit.platform.launcher;
  requires org.assertj.core;
  requires org.junit.jupiter.api;
  requires system.stubs.core;
}
