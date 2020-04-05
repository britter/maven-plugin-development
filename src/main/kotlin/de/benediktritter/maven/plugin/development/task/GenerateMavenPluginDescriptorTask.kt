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

package de.benediktritter.maven.plugin.development.task

import de.benediktritter.maven.plugin.development.internal.MavenLoggerAdapter
import de.benediktritter.maven.plugin.development.internal.MavenServiceFactory
import org.apache.maven.model.Build
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.project.MavenProject
import org.apache.maven.project.artifact.ProjectArtifact
import org.apache.maven.tools.plugin.generator.PluginDescriptorGenerator
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateMavenPluginDescriptorTask : AbstractMavenPluginDevelopmentTask() {

    @get:Input
    abstract val classesDirs: Property<FileCollection>

    @get:Internal
    abstract val javaClassesDir: DirectoryProperty

    @get:Input
    abstract val sourcesDirs: Property<FileCollection>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    private val loggerAdapter = MavenLoggerAdapter(logger)

    private val scanner = MavenServiceFactory.createMojoScanner(loggerAdapter)

    private val generator = PluginDescriptorGenerator(loggerAdapter)

    @TaskAction
    fun generateDescriptor() {
        checkArtifactId()

        val pluginDescriptor = extractPluginDescriptor()
        rewriteHelpMojo(pluginDescriptor)
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

    private fun rewriteHelpMojo(pluginDescriptor: PluginDescriptor) {
        val mavenProject = mavenProject(javaClassesDir.get().asFile)
        HelpGeneratorAccessor.rewriteHelpMojo(createPluginToolsRequest(mavenProject, pluginDescriptor), loggerAdapter)
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

    private fun mavenProject(outputDirectory: File): MavenProject {
        return MavenProject().also {
            it.groupId = project.group.toString()
            it.artifactId = project.name
            it.version = project.version.toString()
            it.artifact = ProjectArtifact(it)
            it.build = Build().also { b ->
                b.outputDirectory = outputDirectory.absolutePath
                b.directory = this@GenerateMavenPluginDescriptorTask.outputDirectory.get().asFile.parent
            }
            // populate compileSourceRoots in order to extract meta data from JavaDoc
            sourcesDirs.get().forEach { dir -> it.addCompileSourceRoot(dir.absolutePath) }
        }
    }
}

