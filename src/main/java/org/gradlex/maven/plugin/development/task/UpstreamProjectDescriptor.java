/*
 * Copyright 2022 the GradleX team.
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

package org.gradlex.maven.plugin.development.task;

import org.gradle.api.tasks.CompileClasspath;
import org.gradle.api.tasks.IgnoreEmptyDirectories;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.work.NormalizeLineEndings;

import java.io.File;

public final class UpstreamProjectDescriptor {

    private final GAV gav;
    private final File classesDir;
    private final File sourceDirectorie;

    public UpstreamProjectDescriptor(GAV gav, File classesDir, File sourcesDir) {
        this.gav = gav;
        this.classesDir = classesDir;
        this.sourceDirectorie = sourcesDir;
    }

    @Nested
    public GAV getGav() {
        return gav;
    }

    @CompileClasspath
    @InputFiles
    public File getClassesDirs() {
        return classesDir;
    }

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    @IgnoreEmptyDirectories
    @NormalizeLineEndings
    public File getSourceDirectories() {
        return sourceDirectorie;
    }
}

