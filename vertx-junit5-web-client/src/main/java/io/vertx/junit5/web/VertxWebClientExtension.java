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
package io.vertx.junit5.web;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

/**
 * JUnit 5 Vert.x Web Client extension that allows parameter injection of WebClient. This extension <b>must</b> be registered after {@link VertxExtension}
 * <p>
 * The following types can be injected:
 * <ul>
 * <li>{@link WebClient}</li>
 * <li>{@link io.vertx.rxjava.ext.web.client.WebClient}</li>
 * <li>{@link io.vertx.reactivex.ext.web.client.WebClient}</li>
 * </ul>
 * <p>
 * If you need to configure {@link WebClientOptions} for your {@link WebClient} you need to declare in your test class a <b>public</b> field
 * with type {@link WebClientOptions} annotated with {@link WebClientOptionsInject}
 *
 * @author <a href="https://slinkydeveloper.com">Francesco Guardiani</a>
 */
public class VertxWebClientExtension implements ParameterResolver {

  private static String WEB_CLIENT = "WebClient";
  private static String RX1_WEB_CLIENT = "Rx1WebClient";
  private static String RX2_WEB_CLIENT = "Rx2WebClient";

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterContext.getParameter().getType();
    return type.equals(WebClient.class) || type.equals(io.vertx.reactivex.ext.web.client.WebClient.class) || type.equals(io.vertx.rxjava.ext.web.client.WebClient.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterContext.getParameter().getType();
    ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.create(VertxWebClientExtension.class, extensionContext));
    if (WebClient.class.equals(type)) {
      return getWebClient(parameterContext, extensionContext, store);
    }
    if (io.vertx.rxjava.ext.web.client.WebClient.class.equals(type)) {
      return getRx1WebClient(parameterContext, extensionContext, store);
    }
    if (io.vertx.reactivex.ext.web.client.WebClient.class.equals(type)) {
      return getRx2WebClient(parameterContext, extensionContext, store);
    }
    throw new IllegalStateException("Looks like the ParameterResolver needs a fix...");
  }

  private WebClient getWebClient(ParameterContext parameterContext, ExtensionContext extensionContext, ExtensionContext.Store myStore) {
    Vertx vertx = VertxExtension.retrieveVertx(parameterContext.getDeclaringExecutable(), extensionContext);
    WebClientOptions webClientOptions = getWebClientOptions(extensionContext).orElse(new WebClientOptions());
    return myStore.getOrComputeIfAbsent(WEB_CLIENT, s -> WebClient.create(vertx, webClientOptions), WebClient.class);
  }

  private io.vertx.rxjava.ext.web.client.WebClient getRx1WebClient(ParameterContext parameterContext, ExtensionContext extensionContext, ExtensionContext.Store myStore) {
    io.vertx.rxjava.core.Vertx vertx = VertxExtension.retrieveRxJava1Vertx(parameterContext.getDeclaringExecutable(), extensionContext);
    WebClientOptions webClientOptions = getWebClientOptions(extensionContext).orElse(new WebClientOptions());
    return myStore.getOrComputeIfAbsent(RX1_WEB_CLIENT, s -> io.vertx.rxjava.ext.web.client.WebClient.create(vertx, webClientOptions), io.vertx.rxjava.ext.web.client.WebClient.class);
  }

  private io.vertx.reactivex.ext.web.client.WebClient getRx2WebClient(ParameterContext parameterContext, ExtensionContext extensionContext, ExtensionContext.Store myStore) {
    io.vertx.reactivex.core.Vertx vertx = VertxExtension.retrieveRxJava2Vertx(parameterContext.getDeclaringExecutable(), extensionContext);
    WebClientOptions webClientOptions = getWebClientOptions(extensionContext).orElse(new WebClientOptions());
    return myStore.getOrComputeIfAbsent(RX2_WEB_CLIENT, s -> io.vertx.reactivex.ext.web.client.WebClient.create(vertx, webClientOptions), io.vertx.reactivex.ext.web.client.WebClient.class);
  }

  private Optional<WebClientOptions> getWebClientOptions(ExtensionContext context) {
    Optional<Class<?>> thisTestClass = context.getTestClass();
    if (!thisTestClass.isPresent()) {
      return Optional.empty();
    }
    List<Field> webClientOptionsField = AnnotationUtils
      .findPublicAnnotatedFields(thisTestClass.get(), WebClientOptions.class, WebClientOptionsInject.class);
    if (webClientOptionsField.isEmpty()) {
      return Optional.empty();
    }
    return ReflectionUtils
      .tryToReadFieldValue(webClientOptionsField.get(0), context.getTestInstance().get())
      .toOptional().map(o -> (WebClientOptions)o);
  }
}
