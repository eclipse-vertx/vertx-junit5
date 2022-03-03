/*
 * Copyright (c) 2021 Red Hat, Inc.
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

import io.vertx.core.Vertx;
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

@ExtendWith(VertxExtension.class)
class RunTestOnContextExampleTest {

  @RegisterExtension
  RunTestOnContext rtoc = new RunTestOnContext();

  Vertx vertx;

  @BeforeEach
  void prepare(VertxTestContext testContext) {
    vertx = rtoc.vertx();
    // Prepare something on a Vert.x event-loop thread
    // The thread changes with each test instance
    testContext.completeNow();
  }

  @Test
  void foo(VertxTestContext testContext) {
    // Test something on the same Vert.x event-loop thread
    // that called prepare
    testContext.completeNow();
  }

  @AfterEach
  void cleanUp(VertxTestContext testContext) {
    // Clean things up on the same Vert.x event-loop thread
    // that called prepare and foo
    testContext.completeNow();
  }
}
