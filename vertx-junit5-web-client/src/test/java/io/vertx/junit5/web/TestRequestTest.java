/*
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.junit5.web;

import io.vertx.core.Future;
import io.vertx.core.Promise;
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
import org.opentest4j.AssertionFailedError;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.vertx.junit5.web.TestRequest.*;
import static org.assertj.core.api.Assertions.assertThat;

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

  @Test
  public void testFailing(WebClient client, Vertx vertx) throws InterruptedException {
    VertxTestContext testContext = new VertxTestContext();

    startHttpServer(vertx, req ->
      req.response()
        .setStatusCode(500)
        .end()
    ).setHandler(testContext.succeeding(h ->
      testRequest(client.get(""))
        .expect(statusCode(200))
        .send(testContext)
    ));

    testContext.awaitCompletion(1, TimeUnit.SECONDS);

    assertThat(testContext.failed()).isTrue();
    assertThat(testContext.causeOfFailure())
      .hasCauseInstanceOf(AssertionFailedError.class);

    assertThat(testContext.causeOfFailure().getStackTrace()[0].getClassName())
      .isEqualTo(this.getClass().getCanonicalName());
  }

  private Future<HttpServer> startHttpServer(Vertx vertx, Consumer<HttpServerRequest> requestHandler) {
    Promise<HttpServer> promise = Promise.promise();
    vertx.createHttpServer().requestHandler(requestHandler::accept).listen(9000, promise);
    return promise.future();
  }

}
