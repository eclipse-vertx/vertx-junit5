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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class OtherExtension implements ParameterResolver {

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterContext.getParameter().getType();
    return type.equals(OtherVertx.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterContext.getParameter().getType();
    ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.create(OtherExtension.class, extensionContext));
    if (OtherVertx.class.equals(type))
      return store.getOrComputeIfAbsent("otherVertx", k -> new OtherVertx(VertxExtension.retrieveVertx(parameterContext.getDeclaringExecutable(), extensionContext)));
    throw new IllegalStateException("Looks like the ParameterResolver needs a fix...");
  }
}
