package io.vertx.junit5;

import io.vertx.core.Vertx;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A supplier of {@link Vertx} instances for use with the {@link ProvidedBy @ProvidedBy} annotation.
 *
 * <p>Implementations must have a public no-arg constructor. The {@link #get()} method (inherited
 * from {@link Supplier}) should create and return the {@code Vertx} instance.
 *
 * <p>Usage example:
 * <pre>{@code
 * public class MyProvider implements VertxProvider {
 *   @Override
 *   public Vertx get() {
 *     return Vertx.vertx(new VertxOptions().setWorkerPoolSize(4));
 *   }
 * }
 * }</pre>
 */
public interface VertxProvider extends Supplier<Vertx> {

  /**
   * Called when the test scope ends to clean up the {@code Vertx} instance.
   *
   * <p>The default implementation calls {@code vertx.close().await()} with the given timeout.
   * Override this method for custom cleanup logic.
   *
   * @param vertx   the {@code Vertx} instance to close
   * @param timeout the maximum duration to wait for the close operation
   * @throws Exception if the close operation fails
   */
  default void close(Vertx vertx, Duration timeout) throws Exception {
    vertx
      .close()
      .await(timeout.toMillis(), TimeUnit.MILLISECONDS);
  }
}
