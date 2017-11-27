/*
 * Copyright (c) 2017 Red Hat, Inc.
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

package io.vertx.ext.junit5;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
class CountingCheckpointTest {

  @Test
  void smoke_test() {
    AtomicBoolean success = new AtomicBoolean(false);
    AtomicReference<Checkpoint> witness = new AtomicReference<>();
    Consumer<Checkpoint> consumer = c -> {
      success.set(true);
      witness.set(c);
    };
    CountingCheckpoint checkpoint = new CountingCheckpoint(consumer, 3);

    checkpoint.flag();
    assertThat(success).isFalse();
    assertThat(witness).hasValue(null);

    checkpoint.flag();
    assertThat(success).isFalse();
    assertThat(witness).hasValue(null);

    checkpoint.flag();
    assertThat(success).isTrue();
    assertThat(witness).hasValue(checkpoint);
  }

  private static final Consumer<Checkpoint> NOOP = c -> {
  };

  @Test
  void refuse_null_satisfaction_trigger() {
    assertThrows(NullPointerException.class, () -> new CountingCheckpoint(null, 1));
  }

  @Test
  void refuse_zero_passes() {
    assertThrows(IllegalArgumentException.class, () -> new CountingCheckpoint(NOOP, 0));
  }

  @Test
  void refuse_negative_passes() {
    assertThrows(IllegalArgumentException.class, () -> new CountingCheckpoint(NOOP, -1));
  }
}
