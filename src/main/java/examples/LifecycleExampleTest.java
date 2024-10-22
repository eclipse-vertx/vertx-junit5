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

import io.vertx.core.VerticleBase;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
@ExtendWith(VertxExtension.class)
class LifecycleExampleTest {

  @BeforeEach
  @DisplayName("Deploy a verticle")
  void prepare(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new SomeVerticle()).onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @DisplayName("A first test")
  void foo(Vertx vertx, VertxTestContext testContext) {
    // (...)
    testContext.completeNow();
  }

  @Test
  @DisplayName("A second test")
  void bar(Vertx vertx, VertxTestContext testContext) {
    // (...)
    testContext.completeNow();
  }

  @AfterEach
  @DisplayName("Check that the verticle is still there")
  void lastChecks(Vertx vertx) {
    assertThat(vertx.deploymentIDs())
      .isNotEmpty()
      .hasSize(1);
  }
}

class SomeVerticle extends VerticleBase {

}
