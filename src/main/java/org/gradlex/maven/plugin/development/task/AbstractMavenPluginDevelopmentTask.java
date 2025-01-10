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

import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.tools.plugin.DefaultPluginToolsRequest;
import org.apache.maven.tools.plugin.PluginToolsRequest;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;

import java.util.stream.Collectors;

public abstract class AbstractMavenPluginDevelopmentTask extends DefaultTask {

    @Nested
    public abstract Property<MavenPluginDescriptor> getPluginDescriptor();

    @Nested
    public abstract ListProperty<DependencyDescriptor> getRuntimeDependencies();

    protected PluginDescriptor createPluginDescriptor() {
        MavenPluginDescriptor pluginDescriptor = getPluginDescriptor().get();

        PluginDescriptor result = new PluginDescriptor();
        result.setGroupId(pluginDescriptor.getGav().getGroup());
        result.setArtifactId(pluginDescriptor.getGav().getArtifactId());
        result.setVersion(pluginDescriptor.getGav().getVersion());
        result.setGoalPrefix(
                pluginDescriptor.getGoalPrefix() != null
                        ? pluginDescriptor.getGoalPrefix()
                        : PluginDescriptor.getGoalPrefixFromArtifactId(pluginDescriptor.getGav().getArtifactId())
        );
        result.setName(pluginDescriptor.getName());
        result.setDescription(pluginDescriptor.getDescription());
        result.setDependencies(getRuntimeDependencies().get().stream()
                .map( DependencyDescriptor::toComponentDependency)
                .collect(Collectors.toList())
        );
        return result;
    }

    protected PluginToolsRequest createPluginToolsRequest(MavenProject mavenProject, PluginDescriptor pluginDescriptor) {
        DefaultPluginToolsRequest request = new DefaultPluginToolsRequest(mavenProject, pluginDescriptor);
        request.setSkipErrorNoDescriptorsFound(true);
        return request;
    }
}
