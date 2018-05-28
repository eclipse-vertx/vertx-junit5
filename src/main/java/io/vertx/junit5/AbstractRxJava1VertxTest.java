package io.vertx.junit5;

import io.vertx.rxjava.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import rx.Completable;
import rx.Single;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Convenient base class to extend when writing asynchronous RxJava 1.x-based Vertx tests.
 */
@ExtendWith(VertxExtension.class)
public abstract class AbstractRxJava1VertxTest extends AbstractRxJavaVertxTest {
  /**
   * Conveniently asynchronously tests a block of code that returns a {@link Single}, then provides the instance wrapped
   * by the {@link Single}, of type <code>T</code>, to a block that can perform assertions on the given instance.
   * This method uses a default timeout of {@link #DEFAULT_TIMEOUT} in units of {@link #DEFAULT_TIMEOUT_TIME_UNIT}.
   *
   * @param context    The {@link VertxTestContext} on which to mark success or failure of the test.
   * @param callable   A function that returns a {@link Single<T>}.
   * @param verifiable A function that takes a <code>T</code> and possibly throws a subclass of {@link Throwable}.
   * @param <T>        The type returned by the function given in param <code>callable</code>.
   * @throws InterruptedException
   */
  public static <T> void testSingle(
    VertxTestContext context,
    Callable<Single<T>> callable,
    Verifiable<T> verifiable) throws InterruptedException {
    testSingle(context, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_TIME_UNIT, callable, verifiable);
  }

  /**
   * Conveniently asynchronously tests a block of code that returns a {@link Single}, then provides the instance wrapped
   * by the {@link Single}, of type <code>T</code>, to a block that can perform assertions on the given instance.
   * This method uses a configurable timeout.
   *
   * @param context    The {@link VertxTestContext} on which to mark success or failure of the test.
   * @param timeout    The timeout value.
   * @param timeUnit   The {@link TimeUnit} of the <code>timeout</code> parameter.
   * @param callable   A function that returns a {@link Single<T>}.
   * @param verifiable A function that takes a <code>T</code> and possibly throws a subclass of {@link Throwable}.
   * @param <T>        The type returned by the function given in param <code>callable</code>.
   * @throws InterruptedException
   */
  public static <T> void testSingle(
    VertxTestContext context,
    long timeout,
    TimeUnit timeUnit,
    Callable<Single<T>> callable,
    Verifiable<T> verifiable) throws InterruptedException {
    try {
      callable.call().subscribe((T it) -> {
        try {
          verifiable.verify(it);
          context.completeNow();
        } catch (Throwable t) {
          context.failNow(t);
        }
      }, context::failNow);
    } catch (Throwable t) {
      context.failNow(t);
    }
    assertTrue(context.awaitCompletion(timeout, timeUnit));
  }

  /**
   * Conveniently asynchronously tests a block of code that returns a {@link Completable}.
   * This method uses a default timeout of {@link #DEFAULT_TIMEOUT} in units of {@link #DEFAULT_TIMEOUT_TIME_UNIT}.
   *
   * @param context  The {@link VertxTestContext} on which to mark success or failure of the test.
   * @param callable A function that returns a {@link Completable}.
   * @throws InterruptedException
   */
  public static void testCompletable(
    VertxTestContext context,
    Callable<Completable> callable) throws InterruptedException {
    testCompletable(context, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_TIME_UNIT, callable);
  }

  /**
   * Conveniently asynchronously tests a block of code that returns a {@link Completable}.
   * This method uses a configurable timeout.
   *
   * @param context  The {@link VertxTestContext} on which to mark success or failure of the test.
   * @param timeout  The timeout value.
   * @param timeUnit The {@link TimeUnit} of the <code>timeout</code> parameter.
   * @param callable A function that returns a {@link Completable}.
   * @throws InterruptedException
   */
  public static void testCompletable(
    VertxTestContext context,
    long timeout,
    TimeUnit timeUnit,
    Callable<Completable> callable) throws InterruptedException {
    try {
      callable.call().subscribe(context::completeNow, context::failNow);
    } catch (Throwable t) {
      context.failNow(t);
    }
    assertTrue(context.awaitCompletion(timeout, timeUnit));
  }
}
