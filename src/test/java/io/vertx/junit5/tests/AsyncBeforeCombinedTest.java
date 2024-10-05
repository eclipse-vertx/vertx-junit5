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
package io.vertx.junit5.tests;

import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
@DisplayName("Test @BeforeEach and @BeforeAll methods")
class AsyncBeforeCombinedTest {

  private static volatile int step;

  @BeforeAll
  static void before_all(VertxTestContext context, Vertx vertx) {
    assertEquals(0, step);
    Checkpoint checkpoint = context.checkpoint();
    vertx.setTimer(200, id -> {
      step = 1;
      checkpoint.flag();
    });
  }

  @BeforeEach
  void before_each(VertxTestContext context, Vertx vertx) {
    assertEquals(1, step);
    Checkpoint checkpoint = context.checkpoint();
    vertx.setTimer(200, id -> {
      step = 2;
      checkpoint.flag();
    });
  }

  @Test
  void check_async_before_completed() {
    assertEquals(2, step);
  }
}
