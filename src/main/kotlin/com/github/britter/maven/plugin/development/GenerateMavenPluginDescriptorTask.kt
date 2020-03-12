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

import com.github.britter.maven.plugin.development.internal.MavenLoggerAdapter
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
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateMavenPluginDescriptorTask : DefaultTask() {

    @get:Input
    abstract val classesDirs: Property<FileCollection>

    @get:Input
    abstract val sourcesDirs: Property<FileCollection>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Nested
    abstract val pluginDescriptor: Property<MavenPluginDescriptor>

    @get:Nested
    abstract val runtimeDependencies: ListProperty<RuntimeDepenencyDescriptor>

    private val loggerAdapter = MavenLoggerAdapter(logger)

    private val scanner = createMojoScanner(loggerAdapter)

    private val generator = PluginDescriptorGenerator(loggerAdapter)

    @TaskAction
    fun generateDescriptor() {
        checkArtifactId()

        val pluginDescriptor = extractPluginDescriptor()

        writeDescriptor(pluginDescriptor)
    }

    private fun checkArtifactId() {
        val artifactId = pluginDescriptor.get().artifactId
        if (artifactId.startsWith("maven-") && artifactId.endsWith("-plugin")) {
            logger.warn("ArtifactIds of the form maven-___-plugin are reserved for plugins of the maven team. Please change the plugin artifactId to the format ___-maven-plugin.")
        }
    }

    private fun writeDescriptor(pluginDescriptor: PluginDescriptor) {
        val mavenProject = mavenProject(outputDirectory.asFile.get())
        generator.execute(outputDirectory.dir("META-INF/maven").get().asFile, createPluginToolsRequest(mavenProject, pluginDescriptor))
    }

    private fun extractPluginDescriptor(): PluginDescriptor {
        return createPluginDescriptor().also { pluginDescriptor ->
            classesDirs.get().forEach {
                val mavenProject = mavenProject(it)
                val pluginToolsRequest = createPluginToolsRequest(mavenProject, pluginDescriptor)
                scanner.populatePluginDescriptor(pluginToolsRequest)
            }
        }
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

    private fun createPluginDescriptor(): PluginDescriptor {
        val pluginDescriptor = pluginDescriptor.get()
        return PluginDescriptor().also {
            it.groupId = pluginDescriptor.groupId
            it.version = pluginDescriptor.version
            val artifactId = pluginDescriptor.artifactId
            it.artifactId = artifactId
            it.goalPrefix = pluginDescriptor.goalPrefix ?: PluginDescriptor.getGoalPrefixFromArtifactId(artifactId)
            it.name = pluginDescriptor.name
            it.description = pluginDescriptor.description
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
            // populate compileSourceRoots in order to extract meta data from JavaDoc
            sourcesDirs.get().forEach { dir -> it.addCompileSourceRoot(dir.absolutePath) }
        }
    }

    private fun getComponentDependencies(): List<ComponentDependency> {
        return runtimeDependencies.get().map { dependency ->
            ComponentDependency().also {
                it.groupId = dependency.groupId
                it.artifactId = dependency.artifactId
                it.version = dependency.version
                it.type = dependency.type
            }
        }
    }
}

data class MavenPluginDescriptor(
        @get:Input val groupId: String,
        @get:Input val artifactId: String,
        @get:Input val version: String,
        @get:Input val name: String,
        @get:Input val description: String,
        @get:Input @get:Optional val goalPrefix: String?
)

data class RuntimeDepenencyDescriptor(
        @get:Input val groupId: String,
        @get:Input val artifactId: String,
        @get:Input val version: String,
        @get:Input val type: String
)
