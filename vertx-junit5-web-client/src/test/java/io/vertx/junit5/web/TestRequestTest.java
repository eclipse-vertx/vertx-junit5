package io.vertx.junit5.web;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.Consumer;

import static io.vertx.junit5.web.TestRequest.*;

@ExtendWith({VertxExtension.class, VertxWebClientExtension.class})
public class TestRequestTest {

  @WebClientOptionsInject
  public WebClientOptions options = new WebClientOptions().setDefaultHost("localhost").setDefaultPort(9000);


  @Test
  public void testSimpleSend(WebClient client, Vertx vertx, VertxTestContext testContext) {
    startHttpServer(vertx, req -> req.response().setStatusCode(200).setStatusMessage("Ciao!").end())
      .setHandler(testContext.succeeding(h -> {
        testRequest(client, HttpMethod.GET, "/path")
          .expect(statusCode(200), statusMessage("Ciao!"))
          .send(testContext);
      }));
  }

  @Test
  public void testJsonAssert(WebClient client, Vertx vertx, VertxTestContext testContext) {
    JsonObject jo = new JsonObject().put("name", "Francesco");
    startHttpServer(vertx, req -> {
      req.response()
        .setStatusCode(200)
        .setStatusMessage("Ciao!")
      .putHeader("content-type", "application/json");
      req.bodyHandler(buf -> req.response().end(buf));
    }).setHandler(testContext.succeeding(h -> {
        testRequest(client, HttpMethod.GET, "/path")
          .expect(jsonBodyResponse(jo))
          .sendJson(jo, testContext);
    }));
  }

  @Test
  public void testHeaders(WebClient client, Vertx vertx, VertxTestContext testContext) {
    startHttpServer(vertx, req ->
      req.response()
        .setStatusCode(200)
        .setStatusMessage("Ciao!")
        .putHeader("x-my-header", req.getHeader("x-my-header"))
        .end()
    ).setHandler(testContext.succeeding(h -> {
      testRequest(client, HttpMethod.GET, "/path")
        .with(requestHeader("x-my-header", "Ciao!"))
        .expect(responseHeader("x-my-header", "Ciao!"))
        .expect(emptyResponse())
        .send(testContext);
    }));
  }

  private Future<HttpServer> startHttpServer(Vertx vertx, Consumer<HttpServerRequest> requestHandler) {
    Future<HttpServer> fut = Future.future();
    vertx.createHttpServer().requestHandler(requestHandler::accept).listen(9000, fut);
    return fut;
  }

}
