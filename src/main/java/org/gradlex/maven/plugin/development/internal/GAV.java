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

package org.gradlex.maven.plugin.development.internal;

import java.util.Objects;

public final class GAV {
    public final String group;
    public final String name;
    public final String version;

    public GAV(String group, String name, String version) {
        this.group = Objects.requireNonNull(group, "Parameter 'group' must not be null");
        this.name = Objects.requireNonNull(name, "Parameter 'name' must not be null");
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GAV gav = (GAV) o;
        return Objects.equals(group, gav.group) && Objects.equals(name, gav.name) && Objects.equals(version, gav.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, name, version);
    }

    @Override
    public String toString() {
        return "GAV{" +
                "group='" + group + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
