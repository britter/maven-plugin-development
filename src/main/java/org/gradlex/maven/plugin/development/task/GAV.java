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

import org.gradle.api.tasks.Input;

import java.util.Objects;

public final class GAV {

    private final String group;
    private final String artifactId;
    private final String version;

    private GAV(String group, String artifactId, String version) {
        this.group = Objects.requireNonNull(group, "Parameter 'group' must not be null");
        this.artifactId = Objects.requireNonNull(artifactId, "Parameter 'artifactId' must not be null");
        this.version = Objects.requireNonNull(version, "Parameter 'version' must not be null");
    }

    public static GAV of(String group, String artifactId, String version) {
        return new GAV(group, artifactId, version);
    }

    @Input
    public String getGroup() {
        return group;
    }

    @Input
    public String getArtifactId() {
        return artifactId;
    }

    @Input
    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GAV gav = (GAV) o;
        return Objects.equals(group, gav.group) && Objects.equals(artifactId, gav.artifactId) && Objects.equals(version, gav.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, artifactId, version);
    }

    @Override
    public String toString() {
        return "GAV{" +
                "group='" + group + '\'' +
                ", name='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
