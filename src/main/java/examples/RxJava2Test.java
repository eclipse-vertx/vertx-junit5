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

import io.vertx.core.Future;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("👀 A RxJava 2 + Vert.x test")
@ExtendWith(VertxExtension.class)
class RxJava2Test {

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    RxHelper.deployVerticle(vertx, new ServerVerticle())
      .subscribe(id -> testContext.completeNow(), testContext::failNow);
  }

  @Test
  @DisplayName("🚀 Start a server and perform requests")
  void server_test(Vertx vertx, VertxTestContext testContext) {
    Checkpoint checkpoints = testContext.strictCheckpoint(10);

    HttpRequest<String> request = WebClient
      .create(vertx)
      .get(8080, "localhost", "/")
      .as(BodyCodec.string());

    request
      .rxSend()
      .repeat(10)
      .subscribe(
        response -> testContext.verify(() -> {
          assertThat(response.body()).isEqualTo("Ok");
          checkpoints.flag();
        }),
        testContext::failNow);
  }

  class ServerVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
      vertx.createHttpServer()
        .requestHandler(req -> {
          System.out.println(req.method() + " " + req.uri() + " from " + req.remoteAddress().host());
          req.response().end("Ok");
        })
        .rxListen(8080)
        .subscribe(server -> startFuture.complete(), startFuture::fail);
    }
  }
}
