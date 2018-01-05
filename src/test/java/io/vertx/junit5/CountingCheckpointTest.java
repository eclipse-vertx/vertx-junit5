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
    CountingCheckpoint checkpoint = CountingCheckpoint.laxCountingCheckpoint(consumer, 3);

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
  void refuse_null_triggers() {
    assertThrows(NullPointerException.class, () -> CountingCheckpoint.laxCountingCheckpoint(null, 1));
    assertThrows(NullPointerException.class, () -> CountingCheckpoint.strictCountingCheckpoint(v -> {
    }, null, 1));
  }

  @Test
  void refuse_zero_passes() {
    assertThrows(IllegalArgumentException.class, () -> CountingCheckpoint.laxCountingCheckpoint(NOOP, 0));
  }

  @Test
  void refuse_negative_passes() {
    assertThrows(IllegalArgumentException.class, () -> CountingCheckpoint.laxCountingCheckpoint(NOOP, -1));
  }

  @Test
  void check_lax_checkpoint() {
    CountingCheckpoint checkpoint = CountingCheckpoint.laxCountingCheckpoint(NOOP, 1);
    checkpoint.flag();
    checkpoint.flag();
  }

  @Test
  void check_strict_checkpoint() {
    AtomicReference<Throwable> box = new AtomicReference<>();
    CountingCheckpoint checkpoint = CountingCheckpoint.strictCountingCheckpoint(NOOP, box::set, 1);

    checkpoint.flag();
    assertThat(box).hasValue(null);
    checkpoint.flag();
    assertThat(box.get())
      .isNotNull()
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Strict checkpoint flagged too many times");
  }
}
