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

package org.gradlex.maven.plugin.development;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Property;

interface MavenPluginDevelopmentExtension {

    String NAME = "mavenPlugin";

     Property<String> getGroupId();

     Property<String> getArtifactId();

     Property<String> getVersion();

     Property<String> getName();

     Property<String> getDescription();

     Property<String> getGoalPrefix();

     Property<String> getHelpMojoPackage();

    /**
     * The set of dependencies to add to the plugin descriptor.
     *
     * Defaults to the runtime classpath of this project.
     */
    Property<Configuration> getDependencies();
}
