/*
 * Copyright 2022 Benedikt Ritter
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

package de.benediktritter.maven.plugin.development.task

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.work.NormalizeLineEndings

data class UpstreamProjectDescriptor(
    @get:Input val group: String,
    @get:Input val name: String,
    @get:Input val version: String,
    @get:[InputFiles CompileClasspath] val classesDirs: FileCollection,
    @get:[InputFiles PathSensitive(PathSensitivity.RELATIVE) IgnoreEmptyDirectories NormalizeLineEndings] val sourceDirectories: FileCollection
)
