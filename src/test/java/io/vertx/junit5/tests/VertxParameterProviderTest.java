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

package io.vertx.junit5.tests;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxParameterProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static io.vertx.junit5.VertxParameterProvider.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.org.webcompere.systemstubs.SystemStubs.restoreSystemProperties;
import static uk.org.webcompere.systemstubs.SystemStubs.withEnvironmentVariable;

/**
 * @author <a href="https://wissel.net/">Stephan Wisssel</a>
 */
@DisplayName("Test of VertxParameterProvider")
public class VertxParameterProviderTest {

  VertxParameterProvider provider = new VertxParameterProvider();
  JsonObject expected = new JsonObject();
  JsonObject actual = new JsonObject();

  @Test
  @DisplayName("Default case - empty VertxOptions")
  void default_empty_options() {
    actual.mergeIn(provider.getVertxOptions());
    assertEquals(expected.encode(), actual.encode(), "Options should be equally empty but are not");
  }

  @Test
  @DisplayName("Failed retrieval of options - env var")
  void failed_retrieval_of_options_env_var() throws Exception {
    failure(true, false);
  }

  @Test
  @DisplayName("Failed retrieval of options - old env var")
  void failed_retrieval_of_options_old_env_var() throws Exception {
    failure(true, true);
  }

  @Test
  @DisplayName("Failed retrieval of options - sys prop")
  void failed_retrieval_of_options_sys_prop() throws Exception {
    failure(false, false);
  }

  private void failure(boolean useEnv, boolean oldEnv) throws Exception {
    String doesNotExist = "something.that.does.not.exist.json";
    if (useEnv) {
      String var = oldEnv ? VERTX_PARAMETER_FILENAME : VERTX_PARAMETER_FILENAME_ENV_VAR;
      withEnvironmentVariable(var, doesNotExist).execute(() -> {
        actual.mergeIn(provider.getVertxOptions());
      });
    } else {
      restoreSystemProperties(() -> {
        System.setProperty(VertxParameterProvider.VERTX_PARAMETER_FILENAME_SYS_PROP, doesNotExist);
        actual.mergeIn(provider.getVertxOptions());
      });
    }
    assertEquals(expected.encode(), actual.encode(), "Options retrieval failure not handled");
  }

  @Test
  @DisplayName("Retrieval of options - env var")
  void retrieval_of_options_env_var(@TempDir Path tempDir) throws Exception {
    success(tempDir, true, false);
  }

  @Test
  @DisplayName("Retrieval of options - old env var")
  void retrieval_of_options_old_env_var(@TempDir Path tempDir) throws Exception {
    success(tempDir, true, true);
  }

  @Test
  @DisplayName("Retrieval of options - sys prop")
  void retrieval_of_options_sys_prop(@TempDir Path tempDir) throws Exception {
    success(tempDir, false, false);
  }

  private void success(Path tempDir, boolean useEnv, boolean oldEnv) throws Exception {
    expected.mergeIn(new JsonObject()
      .put("BlockedThreadCheckInterval", 120)
      .put("MaxWorkerExecuteTime", 42));

    // Create a temp file and populate it with our expected values
    Path tempOptionFile = tempDir.resolve("VertxOptions.json").toAbsolutePath();
    Files.write(tempOptionFile, expected.toBuffer().getBytes());

    if (useEnv) {
      String var = oldEnv ? VERTX_PARAMETER_FILENAME : VERTX_PARAMETER_FILENAME_ENV_VAR;
      withEnvironmentVariable(var, tempOptionFile.toString()).execute(() -> {
        actual.mergeIn(provider.getVertxOptions());
      });
    } else {
      restoreSystemProperties(() -> {
        System.setProperty(VERTX_PARAMETER_FILENAME_SYS_PROP, tempOptionFile.toString());
        actual.mergeIn(provider.getVertxOptions());
      });
    }

    assertEquals(expected.encode(), actual.encode(), "Options retrieval failed");
  }
}
