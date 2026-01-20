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

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A parameter as an object with a scope and that can be closed when the scope exits.
 * <p>
 * This is useful for parameter providers.
 *
 * @param <T> Parameter type
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public class ScopedObject<T> implements Supplier<T>, AutoCloseable {

  private T object;
  private final ParameterClosingConsumer<T> cleaner;

  ScopedObject(T object, ParameterClosingConsumer<T> cleaner) {
    Objects.requireNonNull(object, "The object cannot be null");
    this.object = object;
    this.cleaner = cleaner;
  }

  @Override
  public void close() throws Exception {
    cleaner.accept(object);
  }

  @Override
  public T get() {
    return object;
  }
}
