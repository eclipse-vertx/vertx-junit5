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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.DynamicTestInvocationContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * JUnit 5 Vert.x extension that allows parameter injection as well as an automatic lifecycle on the {@link VertxTestContext} instance.
 * <p>
 * The following types can be injected:
 * <ul>
 * <li>{@link Vertx}</li>
 * <li>{@link VertxTestContext}</li>
 * <li>{@code io.vertx.rxjava.core.Vertx}</li>
 * <li>{@code io.vertx.reactivex.core.Vertx}</li>
 * <li>{@code io.vertx.rxjava3.core.Vertx}</li>
 * </ul>
 *
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public final class VertxExtension implements ParameterResolver, InvocationInterceptor {

  /**
   * Default timeout.
   */
  public static final int DEFAULT_TIMEOUT_DURATION = 30;

  /**
   * Default timeout unit.
   */
  public static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

  /**
   * Key for all {@link Vertx} instances, including what shims like RxJava should use.
   */
  public static final String VERTX_INSTANCE_KEY = "Vertx";

  private static final String TEST_CONTEXT_KEY = "VertxTestContext";

  private static class ContextList extends ArrayList<VertxTestContext> {
    /*
     * There may be concurrent test contexts to join at a point of time because it is allowed to have several
     * user-defined lifecycle event handles (e.g., @BeforeEach, etc).
     */
  }

  private final HashMap<Class<?>, VertxExtensionParameterProvider<?>> parameterProviders = new HashMap<>();

  public VertxExtension() {
    for (VertxExtensionParameterProvider<?> parameterProvider : ServiceLoader.load(VertxExtensionParameterProvider.class)) {
      parameterProviders.put(parameterProvider.type(), parameterProvider);
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return parameterProviders.containsKey(parameterType(parameterContext));
  }

  private Class<?> parameterType(ParameterContext parameterContext) {
    return parameterContext.getParameter().getType();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterType(parameterContext);
    VertxExtensionParameterProvider<?> parameterProvider = parameterProviders.get(type);

    if (type.equals(VertxTestContext.class)) {
      return newTestContext(extensionContext);
    }

    if (extensionContext.getParent().isPresent()) {
      Store parentStore = store(extensionContext.getParent().get());
      if (parentStore.get(parameterProvider.key()) != null) {
        return unpack(parentStore.get(parameterProvider.key()));
      }
    }

    Store store = store(extensionContext);
    return unpack(store.getOrComputeIfAbsent(parameterProvider.key(), key -> new ScopedObject(
      parameterProvider.newInstance(extensionContext, parameterContext),
      parameterProvider.parameterClosingConsumer())));
  }

  private static Object unpack(Object object) {
    if (object instanceof Supplier<?>) {
      return ((Supplier<?>) object).get();
    }
    return object;
  }

  private VertxTestContext newTestContext(ExtensionContext extensionContext) {
    Store store = store(extensionContext);
    ContextList contexts = (ContextList) store.getOrComputeIfAbsent(TEST_CONTEXT_KEY, key -> new ContextList());
    VertxTestContext newTestContext = new VertxTestContext();
    contexts.add(newTestContext);
    return newTestContext;
  }

  private Store store(ExtensionContext extensionContext) {
    return extensionContext.getStore(Namespace.GLOBAL);
  }

  @Override
  public void interceptBeforeAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    invocation.proceed();
    joinActiveTestContexts(extensionContext);
  }

  @Override
  public void interceptAfterAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    invocation.proceed();
    joinActiveTestContexts(extensionContext);
  }

  @Override
  public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    invocation.proceed();
    joinActiveTestContexts(extensionContext);
  }

  @Override
  public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    invocation.proceed();
    joinActiveTestContexts(extensionContext);
  }

  @Override
  public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    invocation.proceed();
    joinActiveTestContexts(extensionContext);
  }

  @Override
  public void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext, ExtensionContext extensionContext) throws Throwable {
    invocation.proceed();
    joinActiveTestContexts(extensionContext);
  }

  @Override
  public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    invocation.proceed();
    joinActiveTestContexts(invocationContext, extensionContext);
  }

  private void joinActiveTestContexts(ExtensionContext extensionContext) throws Exception {
    joinActiveTestContexts(null, extensionContext);
  }

  private void joinActiveTestContexts(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Exception {
    if (extensionContext.getExecutionException().isPresent()) {
      final boolean isNotInAfterEachMethod = Optional.ofNullable(invocationContext)
        .map(ReflectiveInvocationContext::getExecutable)
        .map(executable -> executable.getAnnotation(AfterEach.class))
        .isEmpty();

      if (isNotInAfterEachMethod) {
        return;
      }
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
        } else {
          for (
            Class<?> testClass = extensionContext.getRequiredTestClass();
            testClass != null;
            testClass = testClass.isAnnotationPresent(Nested.class) ? testClass.getEnclosingClass() : null
          ) {
            if (testClass.isAnnotationPresent(Timeout.class)) {
              Timeout annotation = testClass.getAnnotation(Timeout.class);
              timeoutDuration = annotation.value();
              timeoutUnit = annotation.timeUnit();
              break;
            }
          }
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
          String message = "The test execution timed out. Make sure your asynchronous code "
            + "includes calls to either VertxTestContext#completeNow(), VertxTestContext#failNow() "
            + "or Checkpoint#flag()";
          message = message +  context.unsatisfiedCheckpointCallSites()
            .stream()
            .map(element -> String.format("-> checkpoint at %s", element))
            .collect(Collectors.joining("\n", "\n\nUnsatisfied checkpoints diagnostics:\n", ""));
          throw new TimeoutException(message);
        }
      }
    }

    if (extensionContext.getParent().isPresent()) {
      joinActiveTestContexts(extensionContext.getParent().get());
    }
  }
}
