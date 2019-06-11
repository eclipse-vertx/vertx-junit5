package examples;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.web.VertxWebClientExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({
  VertxExtension.class,
  VertxWebClientExtension.class
})
public class SingleAnnotationTest { }
