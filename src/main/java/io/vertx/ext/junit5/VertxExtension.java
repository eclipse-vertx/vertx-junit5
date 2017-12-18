/*
 * Copyright (c) 2017 Red Hat, Inc.
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

package io.vertx.ext.junit5;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public final class VertxExtension implements ParameterResolver, BeforeTestExecutionCallback, AfterTestExecutionCallback {

  private static final int DEFAULT_TIMEOUT_DURATION = 30;
  private static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

  @Override
  public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
    store(extensionContext).put("VertxTestContext.injected", false);
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterType(parameterContext);
    return type == VertxTestContext.class || type == Vertx.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterType(parameterContext);
    if (type == VertxTestContext.class) {
      VertxTestContext testContext = new VertxTestContext();
      store(extensionContext).put("VertxTestContext.injected", true);
      store(extensionContext).put("VertxTestContext", testContext);
      return testContext;
    }
    if (type == Vertx.class) {
      return Vertx.vertx();
    }
    throw new IllegalStateException("Looks like the ParameterResolver needs a fix...");
  }

  @Override
  public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
    if (!store(extensionContext).get("VertxTestContext.injected", Boolean.class)) {
      return;
    }

    VertxTestContext context = store(extensionContext).get("VertxTestContext", VertxTestContext.class);
    store(extensionContext).remove("VertxTestContext");

    int timeoutDuration = DEFAULT_TIMEOUT_DURATION;
    TimeUnit timeoutUnit = DEFAULT_TIMEOUT_UNIT;
    if (extensionContext.getRequiredTestMethod().isAnnotationPresent(Timeout.class)) {
      Timeout annotation = extensionContext.getRequiredTestMethod().getAnnotation(Timeout.class);
      timeoutDuration = annotation.value();
      timeoutUnit = annotation.timeUnit();
    } else if (extensionContext.getRequiredTestClass().isAnnotationPresent(Timeout.class)) {
      Timeout annotation = extensionContext.getRequiredTestClass().getAnnotation(Timeout.class);
      timeoutDuration = annotation.value();
      timeoutUnit = annotation.timeUnit();
    }

    if (!context.awaitCompletion(timeoutDuration, timeoutUnit)) {
      throw new TimeoutException("The test execution timed out");
    }
  }

  private Store store(ExtensionContext extensionContext) {
    return extensionContext.getStore(Namespace.create(getClass(), extensionContext));
  }

  private Class<?> parameterType(ParameterContext parameterContext) {
    return parameterContext.getParameter().getType();
  }
}
