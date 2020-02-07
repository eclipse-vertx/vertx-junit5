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

import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

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
 * @deprecated This extension is not doing anything anymore, the {@link WebClientParameterProvider} class does all the work now.
 */
@Deprecated
public class VertxWebClientExtension implements ParameterResolver {
  
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return false;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    throw new UnsupportedOperationException("All logic has been moved to WebClientParameterProvider");
  }
}
