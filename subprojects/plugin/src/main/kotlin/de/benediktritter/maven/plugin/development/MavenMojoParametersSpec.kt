/*
 * Copyright 2020 Benedikt Ritter
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

package de.benediktritter.maven.plugin.development

import org.gradle.api.Action

/**
 * Configures additional mojo parameters.
 *
 * @since 0.2.0
 */
interface MavenMojoParametersSpec {

    fun parameter(name: String, type: String)

    fun parameter(name: String, type: String, configure: Action<in MavenMojoParameter>)

    fun parameter(name: String, type: Class<Any>)

    fun parameter(name: String, type: Class<Any>, configure: Action<in MavenMojoParameter>)
}
