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

public class VertxTestContextParameterProvider implements VertxExtensionParameterProvider<VertxTestContext> {

  @Override
  public Class<VertxTestContext> type() {
    return VertxTestContext.class;
  }

  @Override
  public String key() {
    doNotCallMe();
    return null;
  }

  @Override
  public VertxTestContext newInstance(ExtensionContext extensionContext, ParameterContext parameterContext) {
    doNotCallMe();
    return null;
  }

  @Override
  public ParameterClosingConsumer<VertxTestContext> parameterClosingConsumer() {
    doNotCallMe();
    return null;
  }

  private ParameterClosingConsumer<VertxTestContext> doNotCallMe() {
    throw new UnsupportedOperationException("VertxTestContext is a built-in special case");
  }
}
