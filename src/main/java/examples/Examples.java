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
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public class Examples {

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

  class BTest {

    @Test
    void start_http_server() throws InterruptedException {
      VertxTestContext testContext = new VertxTestContext();

      Vertx vertx = Vertx.vertx();
      vertx.createHttpServer()
        .requestHandler(req -> req.response().end())
        .listen(16969, testContext.succeeding(ar -> testContext.completeNow())); // <1>

      assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue(); // <2>
    }
  }

  public void usingVerify(Vertx vertx, VertxTestContext testContext) {
    WebClient client = WebClient.create(vertx);

    client.get(8080, "localhost", "/")
      .as(BodyCodec.string())
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.body()).isEqualTo("Plop");
        testContext.completeNow();
      })));
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

    WebClient client = WebClient.create(vertx);
    for (int i = 0; i < 10; i++) {
      client.get(8080, "localhost", "/")
        .as(BodyCodec.string())
        .send(ar -> {
          if (ar.failed()) {
            testContext.failNow(ar.cause());
          } else {
            testContext.verify(() -> assertThat(ar.result().body()).isEqualTo("Ok"));
            responsesReceived.flag();
          }
        });
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

      @RepeatedTest(3)
      void http_server_check_response(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new HttpServerVerticle(), testContext.succeeding(id -> {
          WebClient client = WebClient.create(vertx);
          client.get(8080, "localhost", "/")
            .as(BodyCodec.string())
            .send(testContext.succeeding(response -> testContext.verify(() -> {
              assertThat(response.body()).isEqualTo("Plop");
              testContext.completeNow();
            })));
        }));
      }
    }
  }

  class ETest {

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
