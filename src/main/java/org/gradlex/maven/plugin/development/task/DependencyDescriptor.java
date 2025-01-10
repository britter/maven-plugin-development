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

import org.codehaus.plexus.component.repository.ComponentDependency;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;

public final class DependencyDescriptor {

    private final String type;
    private final GAV gav;

    public DependencyDescriptor(GAV gav, String type) {
        this.gav = gav;
        this.type = type;
    }

    @Nested
    public GAV getGav() {
        return gav;
    }

    @Optional
    @Input
    public String getType() {
        return type;
    }

    public ComponentDependency toComponentDependency() {
        ComponentDependency dep = new ComponentDependency();
        dep.setGroupId(gav.getGroup());
        dep.setArtifactId(gav.getArtifactId());
        dep.setVersion(gav.getVersion());
        dep.setType(type);
        return dep;
    }
}