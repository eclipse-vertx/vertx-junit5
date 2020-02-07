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

package io.vertx.junit5.web;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.ParameterClosingConsumer;
import io.vertx.junit5.ScopedObject;
import io.vertx.junit5.VertxExtensionParameterProvider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WebClientParameterProvider implements VertxExtensionParameterProvider<WebClient> {

  @Override
  public Class<WebClient> type() {
    return WebClient.class;
  }

  @Override
  public String key() {
    return "WebClient";
  }

  @Override
  public WebClient newInstance(ExtensionContext extensionContext, ParameterContext parameterContext) {
    ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);
    ScopedObject scopedObject = store.get("Vertx", ScopedObject.class);
    Objects.requireNonNull(scopedObject, "A Vertx instance must exist, try adding the Vertx parameter as the first method argument");
    Vertx vertx = (Vertx) scopedObject.get();
    WebClientOptions webClientOptions = getWebClientOptions(extensionContext).orElse(new WebClientOptions());
    return WebClient.create(vertx, webClientOptions);
  }

  @Override
  public ParameterClosingConsumer<WebClient> parameterClosingConsumer() {
    return WebClient::close;
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
