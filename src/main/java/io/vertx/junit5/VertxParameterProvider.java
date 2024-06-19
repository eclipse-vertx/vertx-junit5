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
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.junit5.VertxExtension.*;

public class VertxParameterProvider implements VertxExtensionParameterProvider<Vertx> {

  private static final Logger LOG = LoggerFactory.getLogger(VertxParameterProvider.class);

  // Visible for testing
  static final String VERTX_PARAMETER_FILENAME = "vertx.parameter.filename";
  static final String VERTX_PARAMETER_FILENAME_ENV_VAR = "VERTX_PARAMETER_FILENAME";
  static final String VERTX_PARAMETER_FILENAME_SYS_PROP = "vertx.parameter.filename";

  private static final String DEPRECATION_WARNING = String.format(
    "'%s' environment variable is deprecated and will be removed in a future version, use '%s' instead",
    VERTX_PARAMETER_FILENAME,
    VERTX_PARAMETER_FILENAME_ENV_VAR
  );

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
      vertx.close().onComplete(ar -> {
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
    String optionFileName = System.getenv(VERTX_PARAMETER_FILENAME_ENV_VAR);
    if (optionFileName == null) {
      optionFileName = System.getProperty(VERTX_PARAMETER_FILENAME_SYS_PROP);
      if (optionFileName == null) {
        optionFileName = System.getenv(VERTX_PARAMETER_FILENAME);
        if (optionFileName != null) {
          LOG.warn(DEPRECATION_WARNING);
        } else {
          return new JsonObject();
        }
      }
    }
    try {
      Path path = Paths.get(optionFileName);
      Buffer content = Buffer.buffer(Files.readAllBytes(path));
      return new JsonObject(content);
    } catch (Exception e) {
      LOG.warn("Failure when reading Vert.x options file, will use default options", e);
      return new JsonObject();
    }
  }
}
