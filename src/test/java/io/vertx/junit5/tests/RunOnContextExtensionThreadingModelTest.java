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
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.junit5.RunTestOnContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.junit5.tests.StaticRunOnContextExtensionTest.checkContext;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class RunOnContextExtensionThreadingModelTest {

  @RegisterExtension
  RunTestOnContext testOnContext = new RunTestOnContext(ThreadingModel.VIRTUAL_THREAD);

  AtomicReference<Context> ctxRef = new AtomicReference<>();

  @BeforeAll
  static void beforeAll() {
    assertNull(Vertx.currentContext());
  }

  public RunOnContextExtensionThreadingModelTest() {
    assertNull(Vertx.currentContext());
  }

  @BeforeEach
  void beforeTest() {
    Context ctx = Vertx.currentContext();
    assertNotNull(ctx);
    assertSame(ctx.owner(), testOnContext.vertx());
    assertNotSame(ctx, ctxRef.getAndSet(ctx));
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
    assertNull(Vertx.currentContext());
  }
}
