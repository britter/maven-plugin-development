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

import de.benediktritter.maven.plugin.development.internal.MavenPluginDescriptor
import de.benediktritter.maven.plugin.development.task.DependencyDescriptor
import de.benediktritter.maven.plugin.development.task.GenerateHelpMojoSourcesTask
import de.benediktritter.maven.plugin.development.task.GenerateMavenPluginDescriptorTask
import de.benediktritter.maven.plugin.development.task.UpstreamProjectDescriptor
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*

class MavenPluginDevelopmentPlugin : Plugin<Project> {

    companion object {
        const val TASK_GROUP_NAME = "Maven Plugin Development"
    }

    override fun apply(project: Project): Unit = project.run {
        pluginManager.apply(JavaPlugin::class)

        val pluginOutputDirectory = layout.buildDirectory.dir("mavenPlugin")
        val descriptorDir = pluginOutputDirectory.map { it.dir("descriptor") }
        val helpMojoDir = pluginOutputDirectory.map { it.dir("helpMojo") }

        val extension = extensions.create<MavenPluginDevelopmentExtension>(MavenPluginDevelopmentExtension.NAME).also {
            it.groupId.convention(project.provider { project.group.toString() })
            it.artifactId.convention(project.provider { project.name })
            it.version.convention(project.provider { project.version.toString() })
            it.name.convention(project.provider { project.name })
            it.description.convention(project.provider { project.description })
            it.dependencies.convention(configurations["runtimeClasspath"])
        }

        val generateHelpMojoTask = tasks.register<GenerateHelpMojoSourcesTask>("generateMavenPluginHelpMojoSources") {
            group = TASK_GROUP_NAME
            description = "Generates a Maven help mojo that documents the usage of the Maven plugin"

            // capture helpMojoPackage property here for configuration cache compatibility
            val helpMojoPkg = extension.helpMojoPackage
            onlyIf { helpMojoPkg.isPresent }

            helpMojoPackage.set(extension.helpMojoPackage)
            outputDirectory.set(helpMojoDir)
            helpPropertiesFile.set(pluginOutputDirectory.map { it.file("maven-plugin-help.properties") })
            pluginDescriptor.set(project.provider {
                MavenPluginDescriptor(
                        extension.groupId.get(),
                        extension.artifactId.get(),
                        extension.version.get(),
                        extension.name.get(),
                        extension.description.getOrElse(""),
                        extension.goalPrefix.orNull
                )
            })
            runtimeDependencies.set(extension.dependencies.map {
                it.resolvedConfiguration.resolvedArtifacts.map { artifact ->
                    DependencyDescriptor(
                        artifact.moduleVersion.id.group,
                        artifact.moduleVersion.id.name,
                        artifact.moduleVersion.id.version,
                        artifact.extension
                    )
                }
            })
        }

        val main = project.extensions.getByType<SourceSetContainer>()["main"]
        val generateTask = tasks.register<GenerateMavenPluginDescriptorTask>("generateMavenPluginDescriptor") {
            group = TASK_GROUP_NAME
            description = "Generates the Maven plugin descriptor file"

            classesDirs.set(main.output.classesDirs)
            sourcesDirs.set(main.java.sourceDirectories)
            upstreamProjects.convention(provider {
                val compileClasspath = configurations.getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME)
                compileClasspath.incoming.dependencies
                    .filterIsInstance<ProjectDependency>()
                    .map { it.dependencyProject }
                    .map {
                        val mainSourceSet = it.extensions.getByType<SourceSetContainer>()["main"]
                        UpstreamProjectDescriptor(
                            it.group.toString(),
                            it.name,
                            it.version.toString(),
                            mainSourceSet.output.classesDirs,
                            mainSourceSet.java.sourceDirectories
                        )
                    }
            })
            outputDirectory.set(descriptorDir)
            pluginDescriptor.set(project.provider {
                MavenPluginDescriptor(
                        extension.groupId.get(),
                        extension.artifactId.get(),
                        extension.version.get(),
                        extension.name.get(),
                        extension.description.getOrElse(""),
                        extension.goalPrefix.orNull
                )
            })
            runtimeDependencies.set(extension.dependencies.map {
                it.resolvedConfiguration.resolvedArtifacts.map { artifact ->
                    DependencyDescriptor(
                        artifact.moduleVersion.id.group,
                        artifact.moduleVersion.id.name,
                        artifact.moduleVersion.id.version,
                        artifact.extension
                    )
                }
            })

            dependsOn(main.output, generateHelpMojoTask)
        }

        project.afterEvaluate {
            val jarTask: Jar? = tasks.findByName(main.jarTaskName) as Jar?
            jarTask?.from(generateTask)
            main.java.srcDir(generateHelpMojoTask.map { it.outputDirectory })
        }
    }
}
