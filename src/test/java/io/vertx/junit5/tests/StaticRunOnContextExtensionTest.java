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
import io.vertx.core.Vertx;
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({VertxExtension.class})
public class StaticRunOnContextExtensionTest {

  @RegisterExtension
  static RunTestOnContext testOnContext = new RunTestOnContext();

  static AtomicReference<Context> ctxRef = new AtomicReference<>();

  @BeforeAll
  static void beforeAll() {
    Context ctx = Vertx.currentContext();
    assertNotNull(ctx);
    assertSame(ctx.owner(), testOnContext.vertx());
    ctxRef.set(ctx);
  }

  public StaticRunOnContextExtensionTest() {
    assertNull(Vertx.currentContext());
  }

  @BeforeEach
  void beforeTest() {
    checkContext(testOnContext.vertx(), ctxRef.get());
  }

  @Test
  void testMethod1() {
    checkContext(testOnContext.vertx(), ctxRef.get());
  }

  @Test
  void testMethod2() {
    checkContext(testOnContext.vertx(), ctxRef.get());
  }

  @AfterEach
  void tearDown() {
    checkContext(testOnContext.vertx(), ctxRef.get());
  }

  @AfterAll
  static void afterAll() {
    checkContext(testOnContext.vertx(), ctxRef.get());
  }

  static void checkContext(Vertx expectedVertx, Context expectedContext) {
    Context ctx = Vertx.currentContext();
    assertNotNull(ctx);
    assertSame(expectedVertx, ctx.owner());
    assertSame(expectedContext, ctx);
  }
}
