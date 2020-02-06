/*
 * Copyright (c) 2020 Red Hat, Inc.
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
import io.vertx.core.VertxException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.junit5.VertxExtension.DEFAULT_TIMEOUT_DURATION;
import static io.vertx.junit5.VertxExtension.DEFAULT_TIMEOUT_UNIT;

public class VertxParameterProvider implements VertxExtensionParameterProvider<Vertx> {

  @Override
  public Class<Vertx> type() {
    return Vertx.class;
  }

  @Override
  public String key() {
    return "Vertx";
  }

  @Override
  public Vertx newInstance(ExtensionContext extensionContext, ParameterContext parameterContext) {
    return Vertx.vertx();
  }

  @Override
  public ParameterClosingConsumer<Vertx> parameterClosingConsumer() {
    return vertx -> {
      CountDownLatch latch = new CountDownLatch(1);
      AtomicReference<Throwable> errorBox = new AtomicReference<>();
      vertx.close(ar -> {
        if (ar.failed()) {
          errorBox.set(ar.cause());
        }
        latch.countDown();
      });
      if (!latch.await(DEFAULT_TIMEOUT_DURATION, DEFAULT_TIMEOUT_UNIT)) {
        throw new TimeoutException("Closing the Vertx context timed out");
      }
      Throwable throwable = errorBox.get();
      if (throwable != null) {
        if (throwable instanceof Exception) {
          throw (Exception) throwable;
        } else {
          throw new VertxException(throwable);
        }
      }
    };
  }
}
