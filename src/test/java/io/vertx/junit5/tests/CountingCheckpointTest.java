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

package io.vertx.junit5.tests;

import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.CountingCheckpoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
@DisplayName("Unit test for CountingCheckpoint")
class CountingCheckpointTest {

  @Test
  @DisplayName("Smoke tests")
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
    assertThat(checkpoint.satisfied()).isFalse();

    checkpoint.flag();
    assertThat(success).isFalse();
    assertThat(witness).hasValue(null);
    assertThat(checkpoint.satisfied()).isFalse();

    checkpoint.flag();
    assertThat(success).isTrue();
    assertThat(witness).hasValue(checkpoint);
    assertThat(checkpoint.satisfied()).isTrue();
  }

  private static final Consumer<Checkpoint> NOOP = c -> {
  };

  @Test
  @DisplayName("Refuse null triggers")
  void refuse_null_triggers() {
    assertThrows(NullPointerException.class, () -> CountingCheckpoint.laxCountingCheckpoint(null, 1));
    assertThrows(NullPointerException.class, () -> CountingCheckpoint.strictCountingCheckpoint(v -> {
    }, null, 1));
  }

  @Test
  @DisplayName("Refuse having 0 expected passes")
  void refuse_zero_passes() {
    assertThrows(IllegalArgumentException.class, () -> CountingCheckpoint.laxCountingCheckpoint(NOOP, 0));
  }

  @Test
  @DisplayName("Refuse having negative expected passes")
  void refuse_negative_passes() {
    assertThrows(IllegalArgumentException.class, () -> CountingCheckpoint.laxCountingCheckpoint(NOOP, -1));
  }

  @Test
  @DisplayName("Check of a lax checkpoint")
  void check_lax_checkpoint() {
    CountingCheckpoint checkpoint = CountingCheckpoint.laxCountingCheckpoint(NOOP, 1);
    checkpoint.flag();
    checkpoint.flag();
  }

  @Test
  @DisplayName("Check of a strict checkpoint")
  void check_strict_checkpoint() {
    AtomicReference<Throwable> box = new AtomicReference<>();
    CountingCheckpoint checkpoint = CountingCheckpoint.strictCountingCheckpoint(NOOP, box::set, 1);

    assertThat(checkpoint.satisfied()).isFalse();
    checkpoint.flag();
    assertThat(box).hasValue(null);
    assertThat(checkpoint.satisfied()).isTrue();

    checkpoint.flag();
    assertThat(box.get())
      .isNotNull()
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Strict checkpoint flagged too many times");
    assertThat(checkpoint.satisfied()).isTrue();
  }
}
