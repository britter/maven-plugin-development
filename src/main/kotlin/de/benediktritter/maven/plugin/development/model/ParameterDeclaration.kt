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

package de.benediktritter.maven.plugin.development.model

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * representation of a [@Parameter][org.apache.maven.plugins.annotations.Parameter] declaration.
 *
 * @since 0.2.0
 */
open class ParameterDeclaration(@get:Input val name: String, @get:Input val type: String) {

    @get:Input
    @get:Optional
    var description: String? = null

    @get:Input
    @get:Optional
    var alias: String? = null

    @get:Input
    @get:Optional
    var property: String? = null

    @get:Input
    @get:Optional
    var defaultValue: String? = null

    @get:Input
    @get:Optional
    var isRequired: Boolean = false

    @get:Input
    @get:Optional
    var isReadonly: Boolean = false
}
