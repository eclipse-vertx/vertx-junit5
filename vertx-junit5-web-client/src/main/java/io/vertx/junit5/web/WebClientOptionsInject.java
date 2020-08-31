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
package io.vertx.junit5.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to configure {@link io.vertx.ext.web.client.WebClient} with {@link io.vertx.ext.web.client.WebClientOptions} in your test.
 * Look at {@link VertxWebClientExtension} for more details
 *
 * @author <a href="https://slinkydeveloper.com">Francesco Guardiani</a>
 * @deprecated From Vert.x 4 onward this package lives in reactiverse as <a href="https://github.com/reactiverse/reactiverse-junit5-extensions/">reactiverse-junit5-extensions</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface WebClientOptionsInject { }
