package io.vertx.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter annotation used on {@link io.vertx.core.Vertx Vertx} test method or constructor parameters
 * to specify a {@link VertxProvider} implementation that supplies the {@code Vertx} instance.
 *
 * <p>The provider class must have a public no-arg constructor.
 *
 * <p>Usage example:
 * <pre>{@code
 * @Test
 * void myTest(@ProvidedBy(MyProvider.class) Vertx vertx) {
 *   // vertx is created by MyProvider
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface ProvidedBy {

  /**
   * The {@link VertxProvider} implementation class to use for creating the {@code Vertx} instance.
   */
  Class<? extends VertxProvider> value();

}
