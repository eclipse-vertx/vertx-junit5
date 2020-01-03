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

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Checkpoints that count the number of flag invocations.
 *
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
final class CountingCheckpoint implements Checkpoint {

  private final Consumer<Checkpoint> satisfactionTrigger;
  private final Consumer<Throwable> overuseTrigger;
  private final int requiredNumberOfPasses;
  private final StackTraceElement creationCallSite;

  private int numberOfPasses = 0;
  private boolean satisfied = false;

  static CountingCheckpoint laxCountingCheckpoint(Consumer<Checkpoint> satisfactionTrigger, int requiredNumberOfPasses) {
    return new CountingCheckpoint(satisfactionTrigger, null, requiredNumberOfPasses);
  }

  static CountingCheckpoint strictCountingCheckpoint(Consumer<Checkpoint> satisfactionTrigger, Consumer<Throwable> overuseTrigger, int requiredNumberOfPasses) {
    Objects.requireNonNull(overuseTrigger);
    return new CountingCheckpoint(satisfactionTrigger, overuseTrigger, requiredNumberOfPasses);
  }

  private CountingCheckpoint(Consumer<Checkpoint> satisfactionTrigger, Consumer<Throwable> overuseTrigger, int requiredNumberOfPasses) {
    Objects.requireNonNull(satisfactionTrigger);
    if (requiredNumberOfPasses <= 0) {
      throw new IllegalArgumentException("A checkpoint needs at least 1 pass");
    }
    this.creationCallSite = findCallSite();
    this.satisfactionTrigger = satisfactionTrigger;
    this.overuseTrigger = overuseTrigger;
    this.requiredNumberOfPasses = requiredNumberOfPasses;
  }

  private StackTraceElement findCallSite() {
    StackTraceElement[] stackTrace = new Exception().getStackTrace();
    for (int i = stackTrace.length - 1; i >= 0; i--) {
      if (stackTrace[i].getClassName().equals(VertxTestContext.class.getName())) {
        return stackTrace[i + 1];
      }
    }
    return stackTrace[1]; // This can only happen from direct usage of CountingCheckpoint in tests, so the value is irrelevant
  }

  @Override
  public void flag() {
    boolean callSatisfactionTrigger = false;
    boolean callOveruseTrigger = false;
    synchronized (this) {
      if (satisfied) {
        callOveruseTrigger = true;
      } else {
        numberOfPasses = numberOfPasses + 1;
        if (numberOfPasses == requiredNumberOfPasses) {
          callSatisfactionTrigger = true;
          satisfied = true;
        }
      }
    }
    if (callSatisfactionTrigger) {
      satisfactionTrigger.accept(this);
    } else if (callOveruseTrigger && overuseTrigger != null) {
      overuseTrigger.accept(new IllegalStateException("Strict checkpoint flagged too many times"));
    }
  }

  public boolean satisfied() {
    return this.satisfied;
  }

  public StackTraceElement creationCallSite() {
    return creationCallSite;
  }
}
