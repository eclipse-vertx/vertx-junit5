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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.junit5.VertxExtension.*;

public class VertxParameterProvider implements VertxExtensionParameterProvider<Vertx> {

  private static final Logger LOG = LoggerFactory.getLogger(VertxParameterProvider.class);

  // Visible for testing
  public static final String VERTX_PARAMETER_FILENAME = "vertx.parameter.filename";
  public static final String VERTX_PARAMETER_FILENAME_ENV_VAR = "VERTX_PARAMETER_FILENAME";
  public static final String VERTX_PARAMETER_FILENAME_SYS_PROP = "vertx.parameter.filename";

  private static final String DEPRECATION_WARNING = String.format(
    "'%s' environment variable is deprecated and will be removed in a future version, use '%s' instead",
    VERTX_PARAMETER_FILENAME,
    VERTX_PARAMETER_FILENAME_ENV_VAR
  );

  private static final VertxProvider DEFAULT_PROVIDER = new VertxProvider() {

    @Override
    public Vertx get() {
      final JsonObject parameters = getVertxOptions();
      final VertxOptions options = new VertxOptions(parameters);
      return Vertx.vertx(options);
    }
    @Override
    public void close(Vertx vertx, Duration timeout) throws Exception {
      CountDownLatch latch = new CountDownLatch(1);
      AtomicReference<Throwable> errorBox = new AtomicReference<>();
      vertx.close().onComplete(ar -> {
        if (ar.failed()) {
          errorBox.set(ar.cause());
        }
        latch.countDown();
      });
      if (!latch.await(timeout.toMillis(), TimeUnit.SECONDS)) {
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
    }
  };

  @Override
  public Class<Vertx> type() {
    return Vertx.class;
  }

  @Override
  public String key() {
    return VertxExtension.VERTX_INSTANCE_KEY;
  }

  private final WeakHashMap<Vertx, VertxProvider> providers = new WeakHashMap<>();

  @Override
  public Vertx newInstance(ExtensionContext extensionContext, ParameterContext parameterContext) {
    Parameter parameter = parameterContext.getParameter();
    ProvidedBy providedByDecl = parameter.getAnnotation(ProvidedBy.class);
    VertxProvider provider;
    if (providedByDecl != null) {
      Class<? extends VertxProvider> providerClass = providedByDecl.value();
      try {
        provider = providerClass.getConstructor().newInstance();
      } catch (InstantiationException e) {
        throw new VertxException("Provider " + providerClass.getName() + " is not a concrete class");
      } catch (IllegalAccessException e) {
        throw new VertxException("The constructor of " + providerClass.getName() + " is not public");
      } catch (InvocationTargetException e) {
        throw new VertxException("Cound not instantiate " + providerClass.getName(), e.getCause());
      } catch (NoSuchMethodException e) {
        throw new VertxException("Provider " + providerClass.getName() + " does not have a no arg constructor");
      }
    } else {
      provider = DEFAULT_PROVIDER;
    }
    Vertx instance = provider.get();
    if (instance == null) {
      throw new NullPointerException("Provider " + provider.getClass().getName() + " yielded a null value");
    }
    synchronized (providers) {
      providers.put(instance, provider);
    }
    return instance;
  }

  @Override
  public ParameterClosingConsumer<Vertx> parameterClosingConsumer() {
    return vertx -> {
      VertxProvider provider;
      synchronized (providers) {
        provider = providers.remove(vertx);
      }
      if (provider == null) {
        provider = DEFAULT_PROVIDER;
      }
      provider.close(vertx, DEFAULT_TIMEOUT);
    };
  }

  public static JsonObject getVertxOptions() {
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
