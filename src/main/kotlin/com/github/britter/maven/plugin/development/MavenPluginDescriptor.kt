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

package com.github.britter.maven.plugin.development

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

interface MavenPluginDescriptor {

    @get:Input
    val groupId: Property<String>

    @get:Input
    val artifactId: Property<String>

    @get:Input
    val version: Property<String>

    @get:Input
    val name: Property<String>

    @get:Input
    val description: Property<String>

    @get:Input
    @get:Optional
    val goalPrefix: Property<String>
}
