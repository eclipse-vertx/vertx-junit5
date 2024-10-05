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

package io.vertx.junit5.tests;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.RunTestOnContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class CustomizedRunOnContextExtensionTest {

  static AtomicInteger destroyMethodInvocations = new AtomicInteger();
  Vertx expectedVertx;

  @RegisterExtension
  RunTestOnContext testOnContext = new RunTestOnContext(() -> {
    expectedVertx = Vertx.vertx();
    return Future.succeededFuture(expectedVertx);
  }, vertx -> {
    destroyMethodInvocations.incrementAndGet();
    assertSame(expectedVertx, vertx);
    return vertx.close();
  });

  @BeforeEach
  void beforeTest() {
    Context ctx = Vertx.currentContext();
    assertNotNull(ctx);
    assertSame(expectedVertx, ctx.owner());
  }

  @Test
  void testMethod1() {
    Context ctx = Vertx.currentContext();
    assertNotNull(ctx);
    assertSame(expectedVertx, ctx.owner());
  }

  @Test
  void testMethod2() {
    Context ctx = Vertx.currentContext();
    assertNotNull(ctx);
    assertSame(expectedVertx, ctx.owner());
  }

  @AfterEach
  void tearDown() {
    Context ctx = Vertx.currentContext();
    assertNotNull(ctx);
    assertSame(expectedVertx, ctx.owner());
  }

  @AfterAll
  static void afterAll() {
    assertEquals(2, destroyMethodInvocations.get());
  }
}
