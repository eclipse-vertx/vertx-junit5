package io.vertx.junit5;

public class CheckpointFlagOverUseException extends IllegalStateException {
  public CheckpointFlagOverUseException(String s) {
    super(s);
  }
}
