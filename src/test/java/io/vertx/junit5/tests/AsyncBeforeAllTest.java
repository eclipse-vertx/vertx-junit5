/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.junit5.tests;

import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
@DisplayName("Test multiple @BeforeAll methods")
class AsyncBeforeAllTest {

  private static final AtomicBoolean started1 = new AtomicBoolean();
  private static final AtomicBoolean started2 = new AtomicBoolean();
  private static final AtomicInteger count = new AtomicInteger();

  @BeforeAll
  static void before1(VertxTestContext context, Vertx vertx) {
    checkBeforeMethod(context, vertx, started1, started2);
  }

  @BeforeAll
  static void before2(VertxTestContext context, Vertx vertx) {
    checkBeforeMethod(context, vertx, started2, started1);
  }

  private static void checkBeforeMethod(VertxTestContext context, Vertx vertx, AtomicBoolean mine, AtomicBoolean other) {
    int c = count.get();
    if (c == 0) {
      assertFalse(mine.get());
      assertFalse(other.get());
    } else if (c == 1) {
      assertFalse(mine.get());
      assertTrue(other.get());
    }
    Checkpoint checkpoint = context.checkpoint();
    vertx.setTimer(20, id -> {
      mine.set(true);
      count.incrementAndGet();
      checkpoint.flag();
    });
  }

  @Test
  void check_async_before_completed() {
    assertEquals(2, count.get());
    assertTrue(started1.get());
    assertTrue(started2.get());
  }
}
