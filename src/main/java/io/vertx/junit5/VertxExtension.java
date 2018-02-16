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
import io.vertx.core.VertxException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

/**
 * JUnit 5 Vert.x extension that allows the injection of {@link Vertx} and {@link VertxTestContext} parameters as well as
 * an automatic lifecycle on the {@link VertxTestContext} instance.
 *
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public final class VertxExtension implements ParameterResolver, BeforeTestExecutionCallback, AfterTestExecutionCallback, BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

  private static final int DEFAULT_TIMEOUT_DURATION = 30;
  private static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

  private final String TEST_CONTEXT_KEY = "VertxTestContext";
  private final String VERTX_INSTANCE_KEY = "VertxInstance";
  private final String VERTX_INSTANCE_CREATOR_KEY = "VertxInstanceCreator";

  private static class ContextList extends ArrayList<VertxTestContext> {
    /*
     * There may be concurrent test contexts to join at a point of time because it is allowed to have several
     * user-defined lifecycle event handles (e.g., @BeforeEach, etc).
     */
  }

  private static enum VertxInstanceCreator {
    BEFORE_ALL, BEFORE_EACH, TEST
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterType(parameterContext);
    return type == VertxTestContext.class || type == Vertx.class;
  }

  private Class<?> parameterType(ParameterContext parameterContext) {
    return parameterContext.getParameter().getType();
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterType(parameterContext);
    Store store = store(extensionContext);
    if (type == Vertx.class) {
      if (extensionContext.getParent().isPresent()) {
        Store parentStore = store(extensionContext.getParent().get());
        if (parentStore.get(VERTX_INSTANCE_KEY) != null) {
          return parentStore.get(VERTX_INSTANCE_KEY);
        }
      }
      if (store.get(VERTX_INSTANCE_KEY) == null) {
        store.put(VERTX_INSTANCE_CREATOR_KEY, vertxInstanceCreatorFor(parameterContext.getDeclaringExecutable()));
      }
      return store.getOrComputeIfAbsent(VERTX_INSTANCE_KEY, key -> Vertx.vertx());
    }
    if (type == VertxTestContext.class) {
      return newTestContext(store);
    }
    throw new IllegalStateException("Looks like the ParameterResolver needs a fix...");
  }

  private VertxTestContext newTestContext(Store store) {
    ContextList contexts = (ContextList) store.getOrComputeIfAbsent(TEST_CONTEXT_KEY, key -> new ContextList());
    VertxTestContext newTestContext = new VertxTestContext();
    contexts.add(newTestContext);
    return newTestContext;
  }

  private VertxInstanceCreator vertxInstanceCreatorFor(Executable injectionTarget) {
    if (isAnnotated(injectionTarget, BeforeAll.class)) {
      return VertxInstanceCreator.BEFORE_ALL;
    } else if (isAnnotated(injectionTarget, BeforeEach.class)) {
      return VertxInstanceCreator.BEFORE_EACH;
    }
    return VertxInstanceCreator.TEST;
  }

  private Store store(ExtensionContext extensionContext) {
    return extensionContext.getStore(Namespace.create(VertxExtension.class, extensionContext));
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    // Not much we can do here ATM
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    // We may wait on test contexts from @AfterAll methods
    joinActiveTestContexts(context);

    // Cleanup the Vertx instance if created by @BeforeEach in the current context
    checkAndRemoveVertxInstance(context, VertxInstanceCreator.BEFORE_ALL);
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    // We may wait on test contexts from @BeforeAll methods
    joinActiveTestContexts(context);
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    // We may wait on test contexts from @AfterEach methods
    joinActiveTestContexts(context);

    // Cleanup the Vertx instance if created by @BeforeEach in the current context
    checkAndRemoveVertxInstance(context, VertxInstanceCreator.BEFORE_EACH);
  }

  private void checkAndRemoveVertxInstance(ExtensionContext context, VertxInstanceCreator stage) throws Exception {
    Store store = store(context);
    if (store.get(VERTX_INSTANCE_CREATOR_KEY) == stage) {
      store.remove(VERTX_INSTANCE_CREATOR_KEY);
      Vertx vertx = store.remove(VERTX_INSTANCE_KEY, Vertx.class);
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
    }
  }

  @Override
  public void beforeTestExecution(ExtensionContext context) throws Exception {
    // We may wait on test contexts from @BeforeEach methods
    joinActiveTestContexts(context);
  }

  @Override
  public void afterTestExecution(ExtensionContext context) throws Exception {
    // We may wait on the test context from a test
    joinActiveTestContexts(context);

    // Cleanup the Vertx instance if created by the test in the current context
    checkAndRemoveVertxInstance(context, VertxInstanceCreator.TEST);
  }

  private void joinActiveTestContexts(ExtensionContext extensionContext) throws Exception {
    if (extensionContext.getExecutionException().isPresent()) {
      return;
    }

    ContextList currentContexts = store(extensionContext).remove(TEST_CONTEXT_KEY, ContextList.class);
    if (currentContexts != null) {
      for (VertxTestContext context : currentContexts) {
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

    if (extensionContext.getParent().isPresent()) {
      joinActiveTestContexts(extensionContext.getParent().get());
    }
  }
}
