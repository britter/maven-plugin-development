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
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet

interface MavenPluginDevelopmentExtension {

    companion object {
        const val NAME = "mavenPlugin"
    }

    val pluginSourceSet: Property<SourceSet>

    val groupId: Property<String>

    val artifactId: Property<String>

    val version: Property<String>

    val name: Property<String>

    val description: Property<String>

    val goalPrefix: Property<String>

    @Deprecated(
            "Use helpMojoPackage.set(\"org.example.help\") with the desired package for the generated HelpMojo instead",
            ReplaceWith("helpMojoPackage")
    )
    val generateHelpMojo: Property<Boolean>

    val helpMojoPackage: Property<String>

    val mojos: NamedDomainObjectContainer<out MavenMojo>

    fun mojos(action: Action<in NamedDomainObjectContainer<out MavenMojo>>)

    /**
     * The set of dependencies to add to the plugin descriptor.
     *
     * Defaults to the runtime classpath of this projects.
     */
    val dependencies: Property<Configuration>
}
