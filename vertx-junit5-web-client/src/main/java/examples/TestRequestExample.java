package examples;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static io.vertx.junit5.web.TestRequest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestRequestExample {

  @Test
  public void testWithWebClient(WebClient client, VertxTestContext testContext) {
    client
      .get("/hello")
      .addQueryParam("name", "francesco")
      .putHeader("x-my", "foo")
      .send(testContext.succeeding(response -> {
        testContext.verify(() -> {
          assertEquals(200, response.statusCode());
          assertEquals("OK", response.statusMessage());

          Object body = Json.decodeValue(response.bodyAsBuffer());
          String contentTypeHeader = response.getHeader("content-type");
          String otherHeader = response.getHeader("x-my");
          assertEquals(new JsonObject().put("value", "Hello Francesco!"), body);
          assertEquals("application/json", contentTypeHeader);
          assertEquals("bar", otherHeader);
        });
        testContext.completeNow(); // or checkpoint.flag() if you are using Checkpoint
      }));
  }

  @Test
  public void testWithTestRequest(WebClient client, VertxTestContext testContext) {
    testRequest(client, HttpMethod.GET, "/hello")
      .with(queryParam("name", "francesco"), requestHeader("x-my", "foo"))
      .expect(
        jsonBodyResponse(new JsonObject().put("value", "Hello Francesco!")),
        responseHeader("x-my", "bar")
      )
      .send(testContext);
  }

  @Test
  public void testWithTestRequestCheckpoint(WebClient client, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(1);

    testRequest(client, HttpMethod.GET, "/hello")
      .with(queryParam("name", "francesco"), requestHeader("x-my", "foo"))
      .expect(
        jsonBodyResponse(new JsonObject().put("value", "Hello Francesco!")),
        responseHeader("x-my", "bar")
      )
      .send(testContext, checkpoint);
  }

  @Test
  public void testWithTestRequestWrapping(WebClient client, VertxTestContext testContext) {
    testRequest(
        client
          .get("/hello")
          .addQueryParam("name", "francesco")
          .putHeader("x-my", "foo")
      )
      .expect(
        jsonBodyResponse(new JsonObject().put("value", "Hello Francesco!")),
        responseHeader("x-my", "bar")
      )
      .send(testContext);
  }

  @Test
  public void testWithTestRequestCustomAssert(WebClient client, VertxTestContext testContext) {
    // No need to wrap in testContext#verify
    Consumer<HttpResponse<Buffer>> myAssert = req -> assertNotEquals(200, req.statusCode());

    testRequest(client.get("/hello"))
      .expect(
        jsonBodyResponse(new JsonObject().put("value", "Hello Francesco!")),
        myAssert
      )
      .send(testContext);
  }

  @Test
  public void testWithTestRequestChaining(WebClient client, VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);

    testRequest(client, HttpMethod.GET, "/hello")
      .with(queryParam("name", "francesco"), requestHeader("x-my", "foo"))
      .expect(
        jsonBodyResponse(new JsonObject().put("value", "Hello Francesco!")),
        responseHeader("x-my", "bar")
      )
      .send(testContext, checkpoint) // Flag the checkpoint when all asserts succeed
      // Use Future#compose to chain a new request after the end of the first one
      .compose(response1 ->
        testRequest(client, HttpMethod.GET, "/hello")
          .expect(statusCode(200))
          .send(testContext, checkpoint)
      )
      // And another one
      .compose(response2 ->
        testRequest(client, HttpMethod.GET, "/hello")
          .expect(statusCode(500))
          .send(testContext, checkpoint)
      );
  }

}
