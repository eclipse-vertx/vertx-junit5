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
import io.vertx.core.http.HttpMethod;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
@ExtendWith(VertxExtension.class)
public class Examples {

  @ExtendWith(VertxExtension.class)
  class ATest {
    Vertx vertx = Vertx.vertx();

    @Test
    void start_server() {
      vertx.createHttpServer()
        .requestHandler(req -> req.response().end("Ok"))
        .listen(16969, ar -> {
          // (we can check here if the server started or not)
        });
    }
  }

  @Nested
  class ATestNested extends ATest {
    @BeforeEach
    void setVertx(Vertx vertx) {
      this.vertx = vertx;
    }
  }

  @ExtendWith(VertxExtension.class)
  class BTest {
    Vertx vertx = Vertx.vertx();

    @Test
    void start_http_server() throws Throwable {
      VertxTestContext testContext = new VertxTestContext();

      vertx.createHttpServer()
        .requestHandler(req -> req.response().end())
        .listen(16969)
        .onComplete(testContext.succeedingThenComplete()); // <1>

      assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue(); // <2>
      if (testContext.failed()) {  // <3>
        throw testContext.causeOfFailure();
      }
    }
  }

  @Nested
  class BTestNested extends BTest {
    @BeforeEach
    void setVertx(Vertx vertx) {
      this.vertx = vertx;
    }
  }

  @BeforeEach
  void startPlopServer(Vertx vertx, VertxTestContext testContext) {
    vertx.createHttpServer()
      .requestHandler(request -> request.response().end("Plop"))
      .listen(8080, testContext.succeedingThenComplete());
  }

  @Test
  public void usingVerify(Vertx vertx, VertxTestContext testContext) {
    HttpClient client = vertx.createHttpClient();

    client.request(HttpMethod.GET, 8080, "localhost", "/")
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
        assertThat(buffer.toString()).isEqualTo("Plop");
        testContext.completeNow();
      })));
  }

  @Test
  public void checkpointing(Vertx vertx, VertxTestContext testContext) {
    Checkpoint serverStarted = testContext.checkpoint();
    Checkpoint requestsServed = testContext.checkpoint(10);
    Checkpoint responsesReceived = testContext.checkpoint(10);

    vertx.createHttpServer()
      .requestHandler(req -> {
        req.response().end("Ok");
        requestsServed.flag();
      })
      .listen(8888)
      .onComplete(testContext.succeeding(httpServer -> serverStarted.flag()));

    HttpClient client = vertx.createHttpClient();
    for (int i = 0; i < 10; i++) {
      client.request(HttpMethod.GET, 8888, "localhost", "/")
        .compose(req -> req.send().compose(HttpClientResponse::body))
        .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
          assertThat(buffer.toString()).isEqualTo("Ok");
          responsesReceived.flag();
        })));
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

      @Test
      void http_server_check_response(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new HttpServerVerticle(), testContext.succeeding(id -> {
          HttpClient client = vertx.createHttpClient();
          client.request(HttpMethod.GET, 8080, "localhost", "/")
            .compose(req -> req.send().compose(HttpClientResponse::body))
            .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
              assertThat(buffer.toString()).isEqualTo("Plop");
              testContext.completeNow();
            })));
        }));
      }
    }
  }

  @Nested
  class DTestNested extends DTest {
    @Nested
    class SomeTestNested extends DTest.SomeTest {
    }
  }

  class ETest {

    @ExtendWith(VertxExtension.class)
    class SomeTest {

      // Deploy the verticle and execute the test methods when the verticle
      // is successfully deployed
      @BeforeEach
      void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new HttpServerVerticle(), testContext.succeedingThenComplete());
      }

      // Repeat this test 3 times
      @RepeatedTest(3)
      void http_server_check_response(Vertx vertx, VertxTestContext testContext) {
        HttpClient client = vertx.createHttpClient();
        client.request(HttpMethod.GET, 8080, "localhost", "/")
          .compose(req -> req.send().compose(HttpClientResponse::body))
          .onComplete(testContext.succeeding(buffer -> testContext.verify(() -> {
            assertThat(buffer.toString()).isEqualTo("Plop");
            testContext.completeNow();
          })));
      }
    }
  }

  @Nested
  class ETestNested extends ETest {
    @Nested
    class SomeTestNested extends ETest.SomeTest {
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

  static class PTest {

    @ExtendWith(VertxExtension.class)
    static class SomeTest {

      static Stream<Arguments> testData() {
        return Stream.of(
          Arguments.of("complex object1", 4),
          Arguments.of("complex object2", 0)
        );
      }

      @ParameterizedTest
      @MethodSource("testData")
       void test2(String obj, int count, Vertx vertx, VertxTestContext testContext) {
        // your test code
        testContext.completeNow();
      }
    }
  }
}
