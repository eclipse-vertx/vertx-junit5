/*
 * Copyright (c) 2018 Red Hat, Inc.
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
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * JUnit 5 Vert.x extension that allows the injection of {@link Vertx} and {@link VertxTestContext} parameters as well as
 * an automatic lifecycle on the {@link VertxTestContext} instance.
 *
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public final class VertxExtension implements ParameterResolver, BeforeTestExecutionCallback, AfterTestExecutionCallback, BeforeEachCallback, AfterEachCallback {

  private static final int DEFAULT_TIMEOUT_DURATION = 30;
  private static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

  private final String TEST_CONTEXT_KEY = "VertxTestContext";
  private final String VERTX_INSTANCE_KEY = "VertxInstance";

  @Override
  public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
    awaitCheckpoints(extensionContext);
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterType(parameterContext);
    return type == VertxTestContext.class || type == Vertx.class;
  }

  private void awaitCheckpoints(ExtensionContext extensionContext) throws Exception {
    if (extensionContext.getExecutionException().isPresent()) {
      return;
    }
    ContextList list = store(extensionContext).remove(TEST_CONTEXT_KEY, ContextList.class);
    if (list != null) {
      for (VertxTestContext context : list) {
        int timeoutDuration = DEFAULT_TIMEOUT_DURATION;
        TimeUnit timeoutUnit = DEFAULT_TIMEOUT_UNIT;
        Optional<Method> testMethod = extensionContext.getTestMethod();
        if (testMethod.isPresent() && testMethod.get().isAnnotationPresent(Timeout.class)) {
          Timeout annotation = extensionContext.getRequiredTestMethod().getAnnotation(Timeout.class);
          timeoutDuration = annotation.value();
          timeoutUnit = annotation.timeUnit();
        } else if (extensionContext.getRequiredTestClass().isAnnotationPresent(Timeout.class)) {
          Timeout annotation = extensionContext.getRequiredTestClass().getAnnotation(Timeout.class);
          timeoutDuration = annotation.value();
          timeoutUnit = annotation.timeUnit();
        }
        if (context.awaitCompletion(timeoutDuration, timeoutUnit)) {
          if (context.failed()) {
            Throwable throwable = context.causeOfFailure();
            if (throwable instanceof Exception) {
              throw (Exception) throwable;
            } else {
              throw new AssertionError(throwable);
            }
          }
        } else {
          throw new TimeoutException("The test execution timed out");
        }
      }
    }
    Optional<ExtensionContext> parent = extensionContext.getParent();
    if (parent.isPresent()) {
      awaitCheckpoints(parent.get());
    }
  }

  // We use a list because if we have two method beforeEach then we get a single beforeEach callback
  // but we get two VertxTestContext, one for each before method and thus we need to keep track of all of them
  // note that we can only guarantee that the test method will get called after the before each
  // but we cannot guarantee that we don't have concurrent execution between two before each
  private static class ContextList extends ArrayList<VertxTestContext> {

  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterType(parameterContext);
    Store store = store(extensionContext);
    if (type == VertxTestContext.class) {
      VertxTestContext testContext = new VertxTestContext();
      ContextList list = store.get(TEST_CONTEXT_KEY, ContextList.class);
      if (list == null) {
        store.put(TEST_CONTEXT_KEY, list = new ContextList());
      }
      list.add(testContext);
      return testContext;
    }
    if (type == Vertx.class) {
      if (store.get(VERTX_INSTANCE_KEY) == null) {
        store.put(VERTX_INSTANCE_KEY, Vertx.vertx());
      }
      return store.get(VERTX_INSTANCE_KEY);
    }
    throw new IllegalStateException("Looks like the ParameterResolver needs a fix...");
  }

  @Override
  public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
    awaitCheckpoints(extensionContext);
  }

  @Override
  public void beforeEach(ExtensionContext extensionContext) throws Exception {
    awaitCheckpoints(extensionContext);
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    awaitCheckpoints(context);
  }

  private Store store(ExtensionContext extensionContext) {
    return extensionContext.getStore(Namespace.create(getClass(), extensionContext));
  }

  private Class<?> parameterType(ParameterContext parameterContext) {
    return parameterContext.getParameter().getType();
  }
}
