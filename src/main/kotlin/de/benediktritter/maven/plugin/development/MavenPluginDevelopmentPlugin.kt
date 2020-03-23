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

import de.benediktritter.maven.plugin.development.internal.DefaultMavenPluginDevelopmentExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

class MavenPluginDevelopmentPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        pluginManager.apply(JavaBasePlugin::class)

        val pluginOutputDirectory = layout.buildDirectory.dir("mavenPlugin")
        val descriptorDir = pluginOutputDirectory.map { it.dir("descriptor") }
        val helpMojoDir = pluginOutputDirectory.map { it.dir("helpMojo") }

        val extension = createExtension() as DefaultMavenPluginDevelopmentExtension

        val generateHelpMojoTask = tasks.register<GenerateHelpMojoSourcesTask>("generateHelpMojoSources") {
            onlyIf { extension.generateHelpMojo.get() }
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
        }
        // TODO declare help properties as input
        val generateTask = tasks.register<GenerateMavenPluginDescriptorTask>("generateMavenPluginDescriptor") {
            classesDirs.set(extension.pluginSourceSet.map { it.output.classesDirs })
            sourcesDirs.set(extension.pluginSourceSet.map { it.allSource.sourceDirectories })
            javaClassesDir.set(extension.pluginSourceSet.flatMap { it.java.classesDirectory })
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
            runtimeDependencies.set(
                    project.provider {
                        project.configurations["runtimeClasspath"].resolvedConfiguration.resolvedArtifacts.map {
                            RuntimeDependencyDescriptor(
                                    it.moduleVersion.id.group,
                                    it.moduleVersion.id.name,
                                    it.moduleVersion.id.version,
                                    it.extension
                            )
                        }
                    })

            dependsOn(extension.pluginSourceSet.map { it.output }, generateHelpMojoTask)
        }

        project.afterEvaluate {
            val sourceSet = extension.pluginSourceSet.get()
            val jarTask: Jar? = tasks.findByName(sourceSet.jarTaskName) as Jar?
            jarTask?.dependsOn(generateTask)
            jarTask?.from(descriptorDir)
            sourceSet.java.srcDir(helpMojoDir)
            tasks.findByName(sourceSet.jarTaskName)?.dependsOn(generateTask)
            tasks.named(sourceSet.compileJavaTaskName).configure { dependsOn(generateHelpMojoTask) }
        }
    }

    private fun Project.createExtension() =
            extensions.create(
                    MavenPluginDevelopmentExtension::class,
                    MavenPluginDevelopmentExtension.NAME,
                    DefaultMavenPluginDevelopmentExtension::class,
                    this)
}