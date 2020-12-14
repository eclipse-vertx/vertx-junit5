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

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava.ext.web.client.WebClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://slinkydeveloper.com/">Francesco Guardiani</a>
 */
@DisplayName("Test the injection of web client with default options")
@ExtendWith({VertxExtension.class, VertxWebClientExtension.class})
class RxJava1WebClientExtensionCompleteLifecycleInjectionTest {

  @BeforeAll
  static void inTheBeginning(WebClient client, VertxTestContext testContext) {
    assertThat(client).isNotNull();
    assertThat(testContext).isNotNull();
    testContext.completeNow();
  }

  @BeforeEach
  void rightBefore(WebClient client, VertxTestContext testContext) {
    assertThat(client).isNotNull();
    assertThat(testContext).isNotNull();
    testContext.completeNow();
  }

  @Test
  void test(WebClient client, VertxTestContext testContext) {
    assertThat(client).isNotNull();
    assertThat(testContext).isNotNull();
    testContext.completeNow();
  }

  @AfterEach
  void rightAfter(WebClient client, VertxTestContext testContext) {
    assertThat(client).isNotNull();
    assertThat(testContext).isNotNull();
    testContext.completeNow();
  }

  @AfterAll
  static void inTheEnd(WebClient client, VertxTestContext testContext) {
    assertThat(client).isNotNull();
    assertThat(testContext).isNotNull();
    testContext.completeNow();
  }
}
