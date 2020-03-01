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

package com.github.britter.mavenpluginmetadata

import org.apache.maven.model.Build
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.project.MavenProject
import org.apache.maven.project.artifact.ProjectArtifact
import org.apache.maven.tools.plugin.DefaultPluginToolsRequest
import org.apache.maven.tools.plugin.PluginToolsRequest
import org.apache.maven.tools.plugin.extractor.annotations.JavaAnnotationsMojoDescriptorExtractor
import org.apache.maven.tools.plugin.extractor.annotations.scanner.DefaultMojoAnnotationsScanner
import org.apache.maven.tools.plugin.extractor.annotations.scanner.MojoAnnotationsScanner
import org.apache.maven.tools.plugin.generator.PluginDescriptorGenerator
import org.apache.maven.tools.plugin.scanner.DefaultMojoScanner
import org.codehaus.plexus.component.repository.ComponentDependency
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.get

abstract class GenerateMavenPluginDescriptorTask : DefaultTask() {

    @get:InputDirectory
    abstract val classesDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generateDescriptor() {
        val loggerAdapter = MavenLoggerAdapter(logger)
        val annotationsScanner: MojoAnnotationsScanner = DefaultMojoAnnotationsScanner().also {
            it.enableLogging(loggerAdapter)
        }

        val mavenProject = MavenProject().also {
            it.groupId = project.group.toString()
            it.artifactId = project.name
            it.version = project.version.toString()
            it.artifact = ProjectArtifact(it)
            it.build = Build().also { b -> b.outputDirectory = classesDirectory.asFile.get().absolutePath }
        }

        val pluginDescriptor = PluginDescriptor().also {
            it.groupId = project.group.toString()
            it.artifactId = project.name
            it.version = project.version.toString()
            it.goalPrefix = PluginDescriptor.getGoalPrefixFromArtifactId(project.name)
            it.name = project.name
            it.description = project.description
            it.dependencies = getComponentDependencies()
        }

        val pluginToolsRequest: PluginToolsRequest = DefaultPluginToolsRequest(mavenProject, pluginDescriptor)

        val extractor = JavaAnnotationsMojoDescriptorExtractor()
        val extractorClass = extractor.javaClass
        val field = extractorClass.getDeclaredField("mojoAnnotationsScanner")
        field.isAccessible = true
        field.set(extractor, annotationsScanner)

        val scanner = DefaultMojoScanner(mapOf("java-annotations" to extractor)).also {
            it.enableLogging(loggerAdapter)
        }

        scanner.populatePluginDescriptor(pluginToolsRequest)

        val descriptorGenerator = PluginDescriptorGenerator(loggerAdapter)
        // Workaround for the fact that the target location of the help descriptor is derived from the project
        // instead of from the directory passed to execute. This works for Maven since it does not have separate
        // directories for classes and resources in the build output.
        mavenProject.build.outputDirectory = outputDirectory.asFile.get().absolutePath
        descriptorGenerator.execute(outputDirectory.dir("META-INF/maven").get().asFile, pluginToolsRequest)
    }

    private fun getComponentDependencies(): List<ComponentDependency> {
        return project.configurations["runtimeClasspath"].resolvedConfiguration.resolvedArtifacts.map { resolved ->
            ComponentDependency().also {
                it.groupId = resolved.moduleVersion.id.group
                it.artifactId = resolved.moduleVersion.id.name
                it.version = resolved.moduleVersion.id.version
                it.type = resolved.classifier
            }
        }
    }
}
