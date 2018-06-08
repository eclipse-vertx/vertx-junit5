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
import io.vertx.core.impl.VertxThread;
import io.vertx.ext.web.client.WebClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
@DisplayName("Configure extension to run tests in a VertxThread")
class RunOnVertxThreadTest {
  @Test
  public void ensureVertxThread() {
    assertTrue(Thread.currentThread() instanceof VertxThread);
  }

  // NOTE: the code below simulates the heinousness that we're dealing with as a reproable test case
  static class SimulatorOfDumbCodeOutOfOurControl {
    public String WEB_CLIENT_ID;
    public Vertx vertx;

    public SimulatorOfDumbCodeOutOfOurControl(Vertx vertx) {
      this.vertx = vertx;
    }

    public synchronized WebClient getWebClient() {
      WebClient webClient = Vertx.currentContext().get(WEB_CLIENT_ID);
      if (webClient == null) {
        webClient = WebClient.create(vertx);
        Vertx.currentContext().put(WEB_CLIENT_ID, webClient);
      }
      return webClient;
    }
  }

  @Test
  public void reproducibleClientScenario(Vertx vertx, VertxTestContext context)
    throws InterruptedException {
    SimulatorOfDumbCodeOutOfOurControl dumb = new SimulatorOfDumbCodeOutOfOurControl(vertx);
    WebClient webClient = dumb.getWebClient();
    // use webClient here if desired
    assertTrue(context.awaitCompletion(60, TimeUnit.SECONDS));
  }
}
