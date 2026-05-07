package io.vertx.junit5;

import java.util.concurrent.CountDownLatch;

/**
 * A latch counting toward checkpoint completion.
 */
class Latch extends CountDownLatch {

  private final Checkpoint checkpoint;

  Latch(Checkpoint checkpoint, int count) {
    super(count);

    this.checkpoint = checkpoint;
  }

  @Override
  public void countDown() {
    synchronized (this) {
      super.countDown();
      if (getCount() > 0) {
        return;
      }
    }
    checkpoint.succeed();
  }
}
