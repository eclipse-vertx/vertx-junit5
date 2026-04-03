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

import java.time.Duration;

/**
 * A test completion checkpoint, flagging it advances towards the test context completion.
 *
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 * @see VertxTestContext
 */
public interface Checkpoint {

  /**
   * Flags the checkpoint.
   */
  void flag();

  /**
   * Calls {@link #await(Duration)} with a timeout of 2O seconds.
   */
  default void await() {
    await(Duration.ofSeconds(20));
  }

  /**
   * <p>Waits until the checkpoint is satisfied or canceled or the {@code timeout} fires, this can be used
   * to coordinate the overall flow of a test:</p>
   * <ul>
   *   <li>when the checkpoint is satisfied the flow continues and the next statement will be executed</li>
   *   <li>when the test fails, the checkpoint is cancelled and a {@link java.util.concurrent.CancellationException} is thrown</li>
   *   <li>when the timeout fires, {@link java.util.concurrent.TimeoutException} is thrown</li>
   * </ul>
   * <p>This blocks the thread caller, it should never be used from the event-loop, usually it is called from the JUnit
   * thread that coordinates with asynchronous parts of the test.</p>
   *
   * @param timeout the max wait time.
   */
  void await(Duration timeout);
}
