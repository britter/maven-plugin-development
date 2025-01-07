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
import org.apache.maven.artifact.DefaultArtifact
import org.apache.maven.artifact.handler.DefaultArtifactHandler
import org.apache.maven.model.Build
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.project.MavenProject
import org.apache.maven.project.artifact.ProjectArtifact
import org.apache.maven.tools.plugin.generator.PluginDescriptorFilesGenerator
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class GenerateMavenPluginDescriptorTask : AbstractMavenPluginDevelopmentTask() {

    @get:[InputFiles Classpath]
    abstract val classesDirs: ConfigurableFileCollection

    @get:[InputFiles PathSensitive(PathSensitivity.RELATIVE)]
    abstract val sourcesDirs: ConfigurableFileCollection

    @get:Nested
    abstract val upstreamProjects: ListProperty<UpstreamProjectDescriptor>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    private val loggerAdapter = MavenLoggerAdapter(logger)

    private val scanner = MavenServiceFactory.createMojoScanner(loggerAdapter)

    private val generator = PluginDescriptorFilesGenerator()

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
        val mavenProject = mavenProject(sourcesDirs, outputDirectory.asFile.get())
        generator.execute(outputDirectory.dir("META-INF/maven").get().asFile, createPluginToolsRequest(mavenProject, pluginDescriptor))
    }

    private fun extractPluginDescriptor(): PluginDescriptor {
        return createPluginDescriptor().also { pluginDescriptor ->
            classesDirs.forEach { classesDir ->
                val mavenProject = mavenProject(sourcesDirs, classesDir)
                val pluginToolsRequest = createPluginToolsRequest(mavenProject, pluginDescriptor)
                // process upstream projects in order to scan base classes
                upstreamProjects.get().forEach {
                    it.classesDirs.map { classesDir ->
                        val artifact = DefaultArtifact(it.group, it.name,
                            it.version, "compile", "jar", null, DefaultArtifactHandler()).also { artifact ->
                            artifact.file = classesDir
                        }
                        pluginToolsRequest.dependencies.add(artifact)
                        mavenProject.addProjectReference(mavenProject(it.group, it.name, it.version, it.sourceDirectories, classesDir))
                    }
                }
                scanner.populatePluginDescriptor(pluginToolsRequest)
            }
        }
    }

    private fun mavenProject(sourcesDirs: FileCollection, outputDirectory: File): MavenProject =
        mavenProject(pluginDescriptor.get().groupId, pluginDescriptor.get().artifactId, pluginDescriptor.get().version, sourcesDirs, outputDirectory)

    private fun mavenProject(
        groupId: String,
        artifactId: String,
        version: String,
        sourcesDirs: FileCollection,
        outputDirectory: File
    ): MavenProject {
        return MavenProject().also {
            it.groupId = groupId
            it.artifactId = artifactId
            it.version = version
            it.artifact = ProjectArtifact(it)
            it.build = Build().also { b ->
                b.outputDirectory = outputDirectory.absolutePath
                b.directory = this@GenerateMavenPluginDescriptorTask.outputDirectory.get().asFile.parent
            }
            // populate compileSourceRoots in order to extract metadata from JavaDoc
            sourcesDirs.forEach { dir -> it.addCompileSourceRoot(dir.absolutePath) }
        }
    }
}

