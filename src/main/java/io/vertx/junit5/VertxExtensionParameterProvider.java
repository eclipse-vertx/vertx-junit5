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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

/**
 * A {@link VertxExtension} test method parameter provider service provider interface.
 * <p>
 * You can register new providers by pointing to implementations in the
 * {@code META-INF/services/io.vertx.junit5.VertxExtensionParameterProvider} resource.
 *
 * @param <T> Parameter type
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public interface VertxExtensionParameterProvider<T> {

  /**
   * The parameter type.
   *
   * @return the parameter type
   */
  Class<T> type();

  /**
   * A string to identify the parameter in an extension context.
   * <p>
   * In most cases it should be a constant.
   *
   * @return the identifier
   */
  String key();

  /**
   * Provide a new parameter instance.
   *
   * @param extensionContext the extension context
   * @param parameterContext the parameter context
   * @return the new instance
   */
  T newInstance(ExtensionContext extensionContext, ParameterContext parameterContext);

  /**
   * A consumer to close the resource.
   *
   * @return the consumer
   */
  ParameterClosingConsumer<T> parameterClosingConsumer();
}
