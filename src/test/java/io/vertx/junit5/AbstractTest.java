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

package io.vertx.junit5;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractTest {

  public static final AtomicInteger counter = new AtomicInteger();

  @BeforeAll
  static void superBeforeAll(Vertx vertx, VertxTestContext testContext) {
    testContext.verify(() -> expectations(vertx, testContext, 0));
  }

  @BeforeEach
  void superBeforeEach(Vertx vertx, VertxTestContext testContext) {
    testContext.verify(() -> expectations(vertx, testContext, 2));
  }

  protected static void expectations(Vertx vertx, VertxTestContext testContext, int expected) {
    assertEquals(expected, counter.get());
    vertx.setTimer(20, l -> {
      testContext.verify(() -> {
        assertTrue(counter.compareAndSet(expected, expected + 1));
        testContext.completeNow();
      });
    });
  }
}
