package examples;

import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.junit5.web.VertxWebClientExtension;
import io.vertx.junit5.web.WebClientOptionsInject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({
  VertxExtension.class,
  VertxWebClientExtension.class
})
public class WithOptionsTest {

  @WebClientOptionsInject
  public WebClientOptions options = new WebClientOptions()
    .setDefaultHost("example.com")
    .setSsl(true)
    .setDefaultPort(9000);

  @Test
  public void myTest(WebClient webClient, VertxTestContext testContext) {
    webClient.get("/hello"); // GET https://example.com:9000/hello
  }

}
