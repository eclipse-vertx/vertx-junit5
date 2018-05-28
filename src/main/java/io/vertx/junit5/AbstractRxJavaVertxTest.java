package io.vertx.junit5;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.rxjava.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Convenient base class to extend when writing asynchronous RxJava 2.x-based Vertx tests.
 */
@ExtendWith(VertxExtension.class)
public abstract class AbstractRxJavaVertxTest {
  /**
   * Default timeout value for an asynchronous test.
   */
  public static final long DEFAULT_TIMEOUT = 60;

  /**
   * The {@link TimeUnit} of the {@link #DEFAULT_TIMEOUT}.
   */
  public static final TimeUnit DEFAULT_TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;

  /**
   * Functional interface used in conjunction with <code>testSingle()</code> methods.
   */
  @FunctionalInterface
  protected interface Verifiable<T> {
    void verify(T it);
  }

  protected io.vertx.core.Vertx vertx;
  protected Vertx rxvertx;

  @BeforeEach
  public void beforeEach(io.vertx.core.Vertx vertx) {
    this.vertx = vertx;
    this.rxvertx = new Vertx(vertx);
  }

  /**
   * Returns the {@link io.vertx.core.Vertx} instance injected into the test instance.
   */
  public io.vertx.core.Vertx getVertx() {
    return vertx;
  }

  /**
   * Retrns the {@link Vertx} instance constructed from the {@link io.vertx.core.Vertx} instance
   * injected into the test instance.
   */
  public Vertx getRxVertx() {
    return rxvertx;
  }
}
