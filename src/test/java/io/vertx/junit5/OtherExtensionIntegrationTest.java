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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(VertxExtension.class)
@ExtendWith(OtherExtension.class)
public class OtherExtensionIntegrationTest {

  @Test
  public void testParam(Vertx vertx, OtherVertx otherVertx) {
    assertEquals(vertx, otherVertx.vertx);
  }

  @Test
  public void testInvertedParam(OtherVertx otherVertx, Vertx vertx) {
    assertEquals(vertx, otherVertx.vertx);
  }

  @Test
  public void testOnlyOtherVertx(OtherVertx otherVertx) {
    assertNotNull(otherVertx.vertx);
  }
}
