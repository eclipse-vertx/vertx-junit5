package examples;

import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.junit5.web.VertxWebClientExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({
  VertxExtension.class,
  VertxWebClientExtension.class
})
public class SimpleTest {

  @Test
  public void myTest(WebClient webClient, VertxTestContext testContext) {
    // Use the webClient to test your code
  }

}
