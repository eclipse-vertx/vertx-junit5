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

package io.vertx.junit5;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
@ExtendWith(VertxExtension.class)
class VertxExtensionCompleteLifecycleInjectionTest {

  static Vertx daVertx;
  static VertxTestContext daContext;

  @BeforeAll
  static void inTheBeginning(Vertx vertx, VertxTestContext testContext) {
    daVertx = vertx;
    daContext = testContext;
    testContext.completeNow();
  }

  @BeforeEach
  void rightBefore(Vertx vertx, VertxTestContext testContext) {
    assertThat(vertx).isSameAs(daVertx);
    assertThat(testContext).isNotSameAs(daContext);
    testContext.completeNow();
  }

  @Test
  void test1(Vertx vertx, VertxTestContext testContext) {
    assertThat(vertx).isSameAs(daVertx);
    assertThat(testContext).isNotSameAs(daContext);
    testContext.completeNow();
  }

  @Test
  void test2(Vertx vertx, VertxTestContext testContext) {
    assertThat(vertx).isSameAs(daVertx);
    assertThat(testContext).isNotSameAs(daContext);
    testContext.completeNow();
  }

  @AfterEach
  void rightAfter(Vertx vertx, VertxTestContext testContext) {
    assertThat(vertx).isSameAs(daVertx);
    assertThat(testContext).isNotSameAs(daContext);
    testContext.completeNow();
  }

  @AfterAll
  static void inTheEnd(Vertx vertx, VertxTestContext testContext) {
    assertThat(vertx).isSameAs(daVertx);
    assertThat(testContext).isNotSameAs(daContext);
    testContext.completeNow();
  }
}
