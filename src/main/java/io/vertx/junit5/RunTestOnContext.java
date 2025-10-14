/*
 * Copyright (c) 2021 Red Hat, Inc.
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

import io.vertx.core.*;
import io.vertx.core.internal.VertxInternal;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An extension that runs tests on a Vert.x context.
 * <p>
 * When used as a {@link RegisterExtension} instance field, a new {@link Vertx} object and {@link Context} are created for each tested method.
 * {@link org.junit.jupiter.api.BeforeEach} and {@link org.junit.jupiter.api.AfterEach} methods are executed on this context.
 * <p>
 * When used as a {@link RegisterExtension} static field, a single {@link Vertx} object and {@link Context} are created for all the tested methods.
 * {@link org.junit.jupiter.api.BeforeAll} and {@link org.junit.jupiter.api.AfterAll} methods are executed on this context too.
 */
public class RunTestOnContext implements BeforeAllCallback, InvocationInterceptor, AfterEachCallback, AfterAllCallback {

  private volatile boolean staticExtension;

  private volatile Vertx vertx;
  private volatile Context context;

  private final Supplier<Future<Vertx>> supplier;
  private final Function<Vertx, Future<Void>> shutdown;

  /**
   * Create an instance of this extension that builds a {@link Vertx} object using default options.
   */
  public RunTestOnContext() {
    this(new VertxOptions(), false);
  }

  /**
   * Create an instance of this extension that builds a {@link Vertx} object using the specified {@code options}.
   * <p>
   * When the options hold a {@link io.vertx.core.spi.cluster.ClusterManager} instance, a clustered {@link Vertx} object is created.
   *
   * @param options the vertx options
   */
  public RunTestOnContext(VertxOptions options, boolean clustered) {
    this(() -> clustered ? Vertx.clusteredVertx(options) : Future.succeededFuture(Vertx.vertx(options)));
  }

  /**
   * Create an instance of this extension that gets a {@link Vertx} object using the specified asynchronous {@code supplier}.
   *
   * @param supplier the asynchronous supplier
   */
  public RunTestOnContext(Supplier<Future<Vertx>> supplier) {
    this(supplier, Vertx::close);
  }

  /**
   * Create an instance of this extension that gets a {@link Vertx} object using the specified asynchronous {@code supplier}.
   * The asynchronous {@code shutdown} function is invoked when the {@link Vertx} object is no longer needed.
   *
   * @param supplier the asynchronous supplier
   * @param shutdown the asynchronous shutdown function
   */
  public RunTestOnContext(Supplier<Future<Vertx>> supplier, Function<Vertx, Future<Void>> shutdown) {
    this.supplier = supplier;
    this.shutdown = shutdown;
  }

  /**
   * @return the current Vert.x instance
   */
  public Vertx vertx() {
    return vertx;
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    staticExtension = true;
  }

  @Override
  public void interceptBeforeAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    runOnContext(invocation);
  }

  @Override
  public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    runOnContext(invocation);
  }

  @Override
  public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    runOnContext(invocation);
  }

  @Override
  public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    return runOnContext(invocation);
  }

  @Override
  public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    runOnContext(invocation);
  }

  @Override
  public void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext, ExtensionContext extensionContext) throws Throwable {
    runOnContext(invocation);
  }

  @Override
  public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    runOnContext(invocation);
  }

  @Override
  public void interceptAfterAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    runOnContext(invocation);
  }

  private <T> T runOnContext(Invocation<T> invocation) throws Throwable {
    Future<Vertx> vertxFuture;
    if (vertx != null) {
      vertxFuture = Future.succeededFuture(vertx);
    } else {
      vertxFuture = supplier.get().onSuccess(vertx -> {
        this.vertx = vertx;
        context = ((VertxInternal) vertx).createEventLoopContext();
      });
    }
    CompletableFuture<T> cf = vertxFuture
      .compose(ignore -> {
        Promise<T> promise = Promise.promise();
        context.runOnContext(v -> {
          try {
            promise.complete(invocation.proceed());
          } catch (Throwable e) {
            promise.fail(e);
          }
        });
        return promise.future();
      })
      .toCompletionStage()
      .toCompletableFuture();
    try {
      return cf.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw e;
    } catch (ExecutionException e) {
      throw e.getCause();
    }
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    if (staticExtension) {
      return;
    }
    cleanUp();
  }

  private void cleanUp() throws Exception {
    context = null;
    if (vertx == null) {
      return;
    }
    Future<Void> closeFuture = shutdown.apply(vertx);
    vertx = null;
    try {
      closeFuture.toCompletionStage().toCompletableFuture().get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw e;
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof Error) {
        throw (Error) cause;
      }
      if (cause instanceof Exception) {
        throw (Exception) cause;
      }
      throw new RuntimeException(cause);
    }
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    cleanUp();
  }
}
