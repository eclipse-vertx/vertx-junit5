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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class ConcreteTest extends AbstractTest {

  @BeforeAll
  static void beforeAll(Vertx vertx, VertxTestContext testContext) {
    testContext.verify(() -> expectations(vertx, testContext, 1));
  }

  @BeforeEach
  void beforeEach(Vertx vertx, VertxTestContext testContext) {
    testContext.verify(() -> expectations(vertx, testContext, 3));
  }

  @Test
  void testMethod(Vertx vertx, VertxTestContext testContext) {
    testContext.verify(() -> expectations(vertx, testContext, 4));
  }
}
