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

import com.github.britter.maven.plugin.development.internal.DefaultMavenPluginDevelopmentExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

class MavenPluginDevelopmentPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        pluginManager.apply(JavaBasePlugin::class)

        val extension = createExtension(project) as DefaultMavenPluginDevelopmentExtension

        val generateTask = tasks.register<GenerateMavenPluginDescriptorTask>("generateMavenPluginDescriptor") {
            classesDirs.set(extension.pluginSourceSet.map { it.output.classesDirs })
            sourcesDirs.set(extension.pluginSourceSet.map { it.allSource.sourceDirectories } )
            outputDirectory.fileProvider(extension.pluginSourceSet.map { it.output.resourcesDir!! })
            pluginDescriptor.set(MavenPluginDescriptor(
                    extension.groupId.get(),
                    extension.artifactId.get(),
                    extension.version.get(),
                    extension.name.get(),
                    extension.description.get(),
                    extension.goalPrefix.orNull
            ))
            runtimeDependencies.set(
                    project.provider {
                        project.configurations["runtimeClasspath"].resolvedConfiguration.resolvedArtifacts.map {
                            RuntimeDepenencyDescriptor(
                                    it.moduleVersion.id.group,
                                    it.moduleVersion.id.name,
                                    it.moduleVersion.id.version,
                                    it.extension
                            )
                        }
                    })

            dependsOn(extension.pluginSourceSet.map { it.output })
        }

        project.afterEvaluate {
            tasks.findByName(extension.pluginSourceSet.get().jarTaskName)?.dependsOn(generateTask)
        }
    }

    private fun Project.createExtension(project: Project) =
            extensions.create(
                    MavenPluginDevelopmentExtension::class,
                    MavenPluginDevelopmentExtension.NAME,
                    DefaultMavenPluginDevelopmentExtension::class,
                    project)
}
