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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
class VertxExtensionTest {

  @Nested
  @ExtendWith(VertxExtension.class)
  class Injection {

    @Test
    void gimme_vertx(Vertx vertx) {
      assertNotNull(vertx);
    }

    @Test
    void gimme_vertx_test_context(VertxTestContext context) {
      assertNotNull(context);
      context.completeNow();
    }

    @Test
    void gimme_everything(Vertx vertx, VertxTestContext context) {
      assertNotNull(vertx);
      assertNotNull(context);
      context.completeNow();
    }
  }

  @Nested
  @ExtendWith(VertxExtension.class)
  @Timeout(4500)
  class SpecifyTimeout {

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.SECONDS)
    void a(VertxTestContext context) throws InterruptedException {
      Thread.sleep(50);
      context.completeNow();
    }

    @Test
    void b(VertxTestContext context) throws InterruptedException {
      Thread.sleep(50);
      context.completeNow();
    }
  }
}
