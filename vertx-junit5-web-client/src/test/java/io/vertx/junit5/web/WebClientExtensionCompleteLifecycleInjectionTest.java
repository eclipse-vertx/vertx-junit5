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

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://slinkydeveloper.com/">Francesco Guardiani</a>
 */
@DisplayName("Test the injection of web client with default options")
@ExtendWith(VertxExtension.class)
class WebClientExtensionCompleteLifecycleInjectionTest {

  @BeforeAll
  static void inTheBeginning(Vertx vertx, VertxTestContext testContext, WebClient client) {
    assertThat(client).isNotNull();
    assertThat(testContext).isNotNull();
    testContext.completeNow();
  }

  @BeforeEach
  void rightBefore(VertxTestContext testContext, WebClient client) {
    assertThat(client).isNotNull();
    assertThat(testContext).isNotNull();
    testContext.completeNow();
  }

  @Test
  void test(VertxTestContext testContext, WebClient client) {
    assertThat(client).isNotNull();
    assertThat(testContext).isNotNull();
    testContext.completeNow();
  }

  @AfterEach
  void rightAfter(VertxTestContext testContext, WebClient client) {
    assertThat(client).isNotNull();
    assertThat(testContext).isNotNull();
    testContext.completeNow();
  }

  @AfterAll
  static void inTheEnd(VertxTestContext testContext, WebClient client) {
    assertThat(client).isNotNull();
    assertThat(testContext).isNotNull();
    testContext.completeNow();
  }
}
