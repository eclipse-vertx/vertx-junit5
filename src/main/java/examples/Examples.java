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

package examples;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public class Examples {

  @ExtendWith(VertxExtension.class)
  class ATest {

    @Test
    void start_server() {
      Vertx vertx = Vertx.vertx();
      vertx.createHttpServer()
        .requestHandler(req -> req.response().end("Ok"))
        .listen(16969, ar -> {
          // (we can check here if the server started or not)
        });
    }
  }

  @ExtendWith(VertxExtension.class)
  class BTest {

    @Test
    void start_http_server() throws Throwable {
      VertxTestContext testContext = new VertxTestContext();

      Vertx vertx = Vertx.vertx();
      vertx.createHttpServer()
        .requestHandler(req -> req.response().end())
        .listen(16969, testContext.completing()); // <1>

      assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue(); // <2>
      if (testContext.failed()) {  // <3>
        throw testContext.causeOfFailure();
      }
    }
  }

  public void usingVerify(Vertx vertx, VertxTestContext testContext) {
    HttpClient client = vertx.createHttpClient();

    client.get(8080, "localhost", "/")
      .flatMap(HttpClientResponse::body)
      .onSuccess(buffer -> testContext.verify(() -> {
        assertThat(buffer.toString()).isEqualTo("Plop");
        testContext.completeNow();
      }));
  }

  public void checkpointing(Vertx vertx, VertxTestContext testContext) {
    Checkpoint serverStarted = testContext.checkpoint();
    Checkpoint requestsServed = testContext.checkpoint(10);
    Checkpoint responsesReceived = testContext.checkpoint(10);

    vertx.createHttpServer()
      .requestHandler(req -> {
        req.response().end("Ok");
        requestsServed.flag();
      })
      .listen(8080, ar -> {
        if (ar.failed()) {
          testContext.failNow(ar.cause());
        } else {
          serverStarted.flag();
        }
      });

    HttpClient client = vertx.createHttpClient();
    for (int i = 0; i < 10; i++) {
      client.get(8080, "localhost", "/")
        .flatMap(HttpClientResponse::body)
        .onSuccess(buffer -> testContext.verify(() -> assertThat(buffer.toString()).isEqualTo("Ok")))
        .onFailure(testContext::failNow);
    }
  }

  class CTest {

    @ExtendWith(VertxExtension.class)
    class SomeTest {

      @Test
      void some_test(Vertx vertx, VertxTestContext testContext) {
        // (...)
      }
    }
  }

  class HttpServerVerticle extends AbstractVerticle {
  }

  class DTest {

    @ExtendWith(VertxExtension.class)
    class SomeTest {

      void http_server_check_response(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new HttpServerVerticle(), testContext.succeeding(id -> {
          HttpClient client = vertx.createHttpClient();
          client.get(8080, "localhost", "/")
            .flatMap(HttpClientResponse::body)
            .onSuccess(buffer -> testContext.verify(() -> {
              assertThat(buffer.toString()).isEqualTo("Plop");
              testContext.completeNow();
            }));
        }));
      }
    }
  }

  class ETest {

    @ExtendWith(VertxExtension.class)
    class SomeTest {

      // Deploy the verticle and execute the test methods when the verticle is successfully deployed
      @BeforeEach
      void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new HttpServerVerticle(), testContext.completing());
      }

      // Repeat this test 3 times
      @RepeatedTest(3)
      void http_server_check_response(Vertx vertx, VertxTestContext testContext) {
        HttpClient client = vertx.createHttpClient();
        client.get(8080, "localhost", "/")
          .flatMap(HttpClientResponse::body)
          .onSuccess(buffer -> testContext.verify(() -> {
            assertThat(buffer.toString()).isEqualTo("Plop");
            testContext.completeNow();
          }));
      }
    }
  }

  class FTest {

    @ExtendWith(VertxExtension.class)
    class SomeTest {

      @Test
      @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
      void some_test(Vertx vertx, VertxTestContext context) {
        // (...)
      }
    }
  }
}
