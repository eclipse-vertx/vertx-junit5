package io.vertx.junit5;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(VertxExtension.class)
@Inherited
public @interface VertxTest {

  boolean instrumentVertx() default true;

}
