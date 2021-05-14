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

import static io.vertx.junit5.VertxExtension.DEFAULT_TIMEOUT_DURATION;
import static io.vertx.junit5.VertxExtension.DEFAULT_TIMEOUT_UNIT;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

public class VertxParameterProvider implements VertxExtensionParameterProvider<Vertx> {

  public static final String VERTX_PARAMETER_FILENAME = "vertx.parameter.filename";

  @Override
  public Class<Vertx> type() {
    return Vertx.class;
  }

  @Override
  public String key() {
    return VertxExtension.VERTX_INSTANCE_KEY;
  }

  @Override
  public Vertx newInstance(ExtensionContext extensionContext, ParameterContext parameterContext) {

    final JsonObject parameters = this.getVertxOptions();
    final VertxOptions options = new VertxOptions(parameters);
    return Vertx.vertx(options);
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

  public JsonObject getVertxOptions() {
    final JsonObject parameters = new JsonObject();
    final String optionFileName = System.getenv(VERTX_PARAMETER_FILENAME);
    if (optionFileName != null) {
      Path path = Paths.get(optionFileName);
      try {
        final String read = Files.readAllLines(path, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
        parameters.mergeIn(new JsonObject(read));
      } catch (IOException e) {
        // Silently swallowing the error
      }
    }
    return parameters;
  }
}
