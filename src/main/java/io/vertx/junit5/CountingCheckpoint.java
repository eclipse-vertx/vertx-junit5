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
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Checkpoints that count the number of flag invocations.
 *
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public final class CountingCheckpoint implements Checkpoint {

  private static BiConsumer<Checkpoint, Throwable> wrap(Consumer<Checkpoint> consumer) {
    Objects.requireNonNull(consumer);
    return (source, err) -> {
      if (err == null) {
        consumer.accept(source);
      }
    };
  }

  private enum Status {
    OPEN,
    SATISFIED,
    FAILED,
    CANCELLED
  }

  private final BiConsumer<Checkpoint, Throwable> satisfactionTrigger;
  private final Consumer<Throwable> overuseTrigger;
  private final int requiredNumberOfPasses;
  private final StackTraceElement creationCallSite;

  private int numberOfPasses = 0;
  private Status status;

  public static CountingCheckpoint laxCountingCheckpoint(Consumer<Checkpoint> satisfactionTrigger, int requiredNumberOfPasses) {
    return laxCountingCheckpoint(wrap(satisfactionTrigger), requiredNumberOfPasses);
  }

  public static CountingCheckpoint strictCountingCheckpoint(Consumer<Checkpoint> satisfactionTrigger, Consumer<Throwable> overuseTrigger, int requiredNumberOfPasses) {
    Objects.requireNonNull(overuseTrigger);
    return strictCountingCheckpoint(wrap(satisfactionTrigger), overuseTrigger, requiredNumberOfPasses);
  }

  public static CountingCheckpoint laxCountingCheckpoint(BiConsumer<Checkpoint, Throwable> satisfactionTrigger, int requiredNumberOfPasses) {
    return new CountingCheckpoint(satisfactionTrigger, null, requiredNumberOfPasses);
  }

  public static CountingCheckpoint strictCountingCheckpoint(BiConsumer<Checkpoint, Throwable> satisfactionTrigger, Consumer<Throwable> overuseTrigger, int requiredNumberOfPasses) {
    Objects.requireNonNull(overuseTrigger);
    return new CountingCheckpoint(satisfactionTrigger, overuseTrigger, requiredNumberOfPasses);
  }

  private CountingCheckpoint(BiConsumer<Checkpoint, Throwable> completionTrigger, Consumer<Throwable> overuseTrigger, int requiredNumberOfPasses) {
    if (requiredNumberOfPasses <= 0) {
      throw new IllegalArgumentException("A checkpoint needs at least 1 pass");
    }
    this.creationCallSite = findCallSite();
    this.satisfactionTrigger = completionTrigger;
    this.overuseTrigger = overuseTrigger;
    this.requiredNumberOfPasses = requiredNumberOfPasses;
    this.status = Status.OPEN;
  }

  void cancel() {
    synchronized (this) {
      if (status != Status.OPEN) {
        return;
      }
      status = Status.CANCELLED;
      notifyAll();
    }
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
  public CountDownLatch asLatch(int count) {
    return new Latch(this, count);
  }

  @Override
  public void complete(Object result, Throwable failure) {
    if (failure != null) {
      synchronized (this) {
        if (status != Status.OPEN) {
          return;
        }
        status = Status.FAILED;
        notifyAll();
      }
      satisfactionTrigger.accept(this, failure);
    } else {
      flag();
    }
  }

  @Override
  public void flag() {
    boolean callSatisfactionTrigger = false;
    boolean callOveruseTrigger = false;
    synchronized (this) {
      switch (status) {
        case OPEN:
          numberOfPasses = numberOfPasses + 1;
          if (numberOfPasses == requiredNumberOfPasses) {
            callSatisfactionTrigger = true;
            status = Status.SATISFIED;
            notifyAll();
          }
          break;
        case SATISFIED:
          callOveruseTrigger = true;
          break;
        default:
          return;
      }
    }
    if (callSatisfactionTrigger) {
      satisfactionTrigger.accept(this, null);
    } else if (callOveruseTrigger && overuseTrigger != null) {
      overuseTrigger.accept(new IllegalStateException("Strict checkpoint flagged too many times"));
    }
  }

  @Override
  public void await(Duration timeout) {
    if (timeout.isZero() || timeout.isNegative()) {
      throw new IllegalArgumentException("Invalid timeout");
    }
    synchronized (this) {
      if (status == Status.OPEN) {
        try {
          wait(timeout.toMillis());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throwEx(e);
        }
        if (status == Status.CANCELLED) {
          throwEx(new CancellationException("Test failed"));
        }
        if (status == Status.FAILED) {
          throwEx(new Exception("Checkpoint failed"));
        }
        if (status != Status.SATISFIED) {
          throwEx(new TimeoutException());
        }
      }
    }
  }

  public synchronized boolean satisfied() {
    return status == Status.SATISFIED;
  }

  public StackTraceElement creationCallSite() {
    return creationCallSite;
  }

  private static <E extends Throwable> void throwEx(Throwable t) throws E {
    throw (E) t;
  }
}
