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
import org.apache.maven.tools.plugin.extractor.MojoDescriptorExtractor
import org.apache.maven.tools.plugin.extractor.annotations.JavaAnnotationsMojoDescriptorExtractor
import org.apache.maven.tools.plugin.extractor.annotations.scanner.DefaultMojoAnnotationsScanner
import org.apache.maven.tools.plugin.extractor.annotations.scanner.MojoAnnotationsScanner
import org.apache.maven.tools.plugin.generator.PluginDescriptorGenerator
import org.apache.maven.tools.plugin.scanner.DefaultMojoScanner
import org.apache.maven.tools.plugin.scanner.MojoScanner
import org.codehaus.plexus.component.repository.ComponentDependency
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import java.io.File

abstract class GenerateMavenPluginDescriptorTask : DefaultTask() {

    @get:Input
    abstract val classesDirs: Property<FileCollection>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Nested
    abstract val pluginDescriptor: Property<MavenPluginDescriptor>

    private val loggerAdapter = MavenLoggerAdapter(logger)

    private val scanner = createMojoScanner(loggerAdapter)

    @TaskAction
    fun generateDescriptor() {
        val pluginDescriptor = pluginDescriptor()
        classesDirs.get().forEach {
            val mavenProject = mavenProject(it)
            val pluginToolsRequest = createPluginToolsRequest(mavenProject, pluginDescriptor)
            scanner.populatePluginDescriptor(pluginToolsRequest)
        }
        val descriptorGenerator = PluginDescriptorGenerator(loggerAdapter)
        // Workaround for the fact that the target location of the help descriptor is derived from the project
        // instead of from the directory passed to execute. This works for Maven since it does not have separate
        // directories for classes and resources in the build output.
        val mavenProject = mavenProject(outputDirectory.asFile.get())
        descriptorGenerator.execute(outputDirectory.dir("META-INF/maven").get().asFile, createPluginToolsRequest(mavenProject, pluginDescriptor))
    }

    private fun createMojoScanner(loggerAdapter: MavenLoggerAdapter): MojoScanner {
        val extractor = createMojoDescriptorExtractor(loggerAdapter)

        return DefaultMojoScanner(mapOf("java-annotations" to extractor)).also {
            it.enableLogging(loggerAdapter)
        }
    }

    private fun createMojoDescriptorExtractor(loggerAdapter: MavenLoggerAdapter): MojoDescriptorExtractor {
        val annotationsScanner: MojoAnnotationsScanner = DefaultMojoAnnotationsScanner().also {
            it.enableLogging(loggerAdapter)
        }

        val extractor = JavaAnnotationsMojoDescriptorExtractor()
        val extractorClass = extractor.javaClass
        val field = extractorClass.getDeclaredField("mojoAnnotationsScanner")
        field.isAccessible = true
        field.set(extractor, annotationsScanner)
        return extractor
    }

    private fun createPluginToolsRequest(mavenProject: MavenProject, pluginDescriptor: PluginDescriptor): PluginToolsRequest {
        return DefaultPluginToolsRequest(mavenProject, pluginDescriptor).also {
            it.isSkipErrorNoDescriptorsFound = true
        }
    }

    private fun pluginDescriptor(): PluginDescriptor {
        val pluginDescriptor = pluginDescriptor.get()
        return PluginDescriptor().also {
            it.groupId = pluginDescriptor.groupId.get()
            it.version = pluginDescriptor.version.get()
            val artifactId = pluginDescriptor.artifactId.get()
            it.artifactId = artifactId
            it.goalPrefix = pluginDescriptor.goalPrefix.getOrElse(PluginDescriptor.getGoalPrefixFromArtifactId(artifactId))
            it.name = pluginDescriptor.name.get()
            it.description = pluginDescriptor.description.get()
            it.dependencies = getComponentDependencies()
        }
    }

    private fun mavenProject(outputDirectory: File): MavenProject {
        return MavenProject().also {
            it.groupId = project.group.toString()
            it.artifactId = project.name
            it.version = project.version.toString()
            it.artifact = ProjectArtifact(it)
            it.build = Build().also { b -> b.outputDirectory = outputDirectory.absolutePath }
        }
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
