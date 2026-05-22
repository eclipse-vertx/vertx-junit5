package io.vertx.junit5;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Annotation for JUnit 5 test classes that use Vert.x. Applying this annotation registers the
 * {@link VertxExtension}, which manages {@link io.vertx.core.Vertx Vertx} and
 * {@link VertxTestContext} instances for parameter injection.
 *
 * <p>By default, any {@code Vertx} instance created by the extension and injected into test
 * methods or constructors is <em>instrumented</em>: its
 * {@linkplain io.vertx.core.Vertx#exceptionHandler exception handler} is set to
 * {@link VertxTestContext#failNow(Throwable)}, so any unhandled exception occurring inside
 * Vert.x (e.g. in a handler) automatically fails the test. This is
 * equivalent to wrapping handler code in {@link VertxTestContext#verify}, which catches
 * exceptions and calls {@code failNow} as well. Instrumentation saves you from having to
 * wrap every callback with {@code verify}; you can disable it by setting
 * {@link #instrumentVertx()} to {@code false}.
 *
 * <p>This annotation is {@link Inherited @Inherited}, so subclasses of an annotated test class
 * are also treated as Vert.x tests.
 *
 * <p>Usage example:
 * <pre>{@code
 * @VertxTest
 * class MyTest {
 *   @Test
 *   void myTest(Vertx vertx, VertxTestContext ctx) {
 *     // No need to wrap with ctx.verify(), unhandled exceptions
 *     // in Vert.x handlers will automatically fail the test.
 *   }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(VertxExtension.class)
@Inherited
public @interface VertxTest {

  /**
   * Whether to instrument the {@code Vertx} instance by setting its exception handler to
   * {@link VertxTestContext#failNow(Throwable)}, so that unhandled exceptions automatically
   * fail the test. Defaults to {@code true}.
   *
   * @return {@code true} to instrument the {@code Vertx} instance, {@code false} otherwise
   */
  boolean instrumentVertx() default true;

}
