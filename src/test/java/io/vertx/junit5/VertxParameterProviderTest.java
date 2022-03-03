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

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="https://wissel.net/">Stephan Wisssel</a>
 */
@DisplayName("Test of VertxParameterProvider")
public class VertxParameterProviderTest {

  @Test
  @DisplayName("Default case - empty VertxOptions")
  void default_empty_options() {
    VertxParameterProvider provider = new VertxParameterProvider();
    JsonObject expected = new JsonObject();
    JsonObject actual = provider.getVertxOptions();
    assertEquals(expected.encode(), actual.encode(), "Options should be equally empty but are not");
  }

  @Test
  @DisplayName("Failed retrieval of options")
  void failed_retrieval_of_options() throws Exception {
    VertxParameterProvider provider = new VertxParameterProvider();
    final JsonObject expected = new JsonObject();
    final JsonObject actual = new JsonObject();

    withEnvironmentVariable(VertxParameterProvider.VERTX_PARAMETER_FILENAME, "something.that.does.not.exist.json")
      .execute(() -> {
        actual.mergeIn(provider.getVertxOptions());
      });

    assertEquals(expected.encode(), actual.encode(), "Options retrival failure not handled");
  }

  @Test
  @DisplayName("Retrieval of options")
  void retrieval_of_options() throws Exception {
    VertxParameterProvider provider = new VertxParameterProvider();
    final JsonObject expected = new JsonObject().put("BlockedThreadCheckInterval", 120).put("MaxWorkerExecuteTime",
      42);
    final JsonObject actual = new JsonObject();
    // Create a temp file and populate it with our expected values
    File tempOptionFile = File.createTempFile("VertxOptions-", ".json");
    tempOptionFile.deleteOnExit();
    BufferedWriter writer = new BufferedWriter(new FileWriter(tempOptionFile.getAbsolutePath()));
    writer.write(expected.encode());
    writer.close();

    withEnvironmentVariable(VertxParameterProvider.VERTX_PARAMETER_FILENAME, tempOptionFile.getAbsolutePath())
      .execute(() -> {
        actual.mergeIn(provider.getVertxOptions());
      });

    assertEquals(expected.encode(), actual.encode(), "Options retrival failed");
  }

}
