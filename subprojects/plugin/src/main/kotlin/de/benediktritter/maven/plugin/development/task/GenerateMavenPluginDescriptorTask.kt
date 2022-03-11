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
import de.benediktritter.maven.plugin.development.internal.DefaultMavenMojo
import de.benediktritter.maven.plugin.development.internal.DefaultMavenMojoParameter
import org.apache.maven.artifact.DefaultArtifact
import org.apache.maven.artifact.handler.DefaultArtifactHandler
import org.apache.maven.model.Build
import org.apache.maven.plugin.descriptor.MojoDescriptor
import org.apache.maven.plugin.descriptor.Parameter
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.project.MavenProject
import org.apache.maven.project.artifact.ProjectArtifact
import org.apache.maven.tools.plugin.ExtendedMojoDescriptor
import org.apache.maven.tools.plugin.generator.PluginDescriptorGenerator
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import java.io.File

@CacheableTask
abstract class GenerateMavenPluginDescriptorTask : AbstractMavenPluginDevelopmentTask() {

    @get:Input
    abstract val classesDirs: Property<FileCollection>

    @get:Internal
    abstract val javaClassesDir: DirectoryProperty

    @get:Input
    abstract val sourcesDirs: Property<FileCollection>

    @get:Nested
    abstract val upstreamProjects: ListProperty<UpstreamProjectDescriptor>

    @get:Nested
    abstract val additionalMojos: SetProperty<DefaultMavenMojo>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    private val loggerAdapter = MavenLoggerAdapter(logger)

    private val scanner = MavenServiceFactory.createMojoScanner(loggerAdapter)

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
        val mavenProject = mavenProject(project, sourcesDirs.get(), outputDirectory.asFile.get())
        generator.execute(outputDirectory.dir("META-INF/maven").get().asFile, createPluginToolsRequest(mavenProject, pluginDescriptor))
    }

    private fun extractPluginDescriptor(): PluginDescriptor {
        return createPluginDescriptor().also { pluginDescriptor ->
            classesDirs.get().forEach { classesDir ->
                val mavenProject = mavenProject(project, sourcesDirs.get(), classesDir)
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
            // process upstream projects again in order to find mojo implementations
            upstreamProjects.get().forEach {
                it.classesDirs.forEach { classesDir ->
                    val mavenProject = mavenProject(it.group, it.name, it.version, it.sourceDirectories, classesDir)
                    val pluginToolsRequest = createPluginToolsRequest(mavenProject, pluginDescriptor)
                    scanner.populatePluginDescriptor(pluginToolsRequest)
                }
            }
            addAdditionalMojos(pluginDescriptor)
        }
    }

    private fun addAdditionalMojos(pluginDescriptor: PluginDescriptor) {
        additionalMojos.get().map(::toMojoDescriptor).forEach { pluginDescriptor.addMojo(it) }
    }

    private fun toMojoDescriptor(mojo: DefaultMavenMojo): MojoDescriptor {
        return ExtendedMojoDescriptor().also {
            it.goal = mojo.name
            it.description = mojo.description
            it.implementation = mojo.implementation
            it.language = mojo.language
            it.phase = mojo.defaultPhase.id()
            it.dependencyResolutionRequired = mojo.requiresDependencyResolution.id()
            it.dependencyCollectionRequired = mojo.requiresDependencyCollection.id()
            it.instantiationStrategy = mojo.instantiationStrategy.id()
            it.executionStrategy = mojo.executionStrategy.id()
            it.isProjectRequired = mojo.isRequiresProject
            it.isRequiresReports = mojo.isRequiresReports
            it.isAggregator = mojo.isAggregator
            it.isDirectInvocationOnly = mojo.isRequiresDirectInvocation
            it.isOnlineRequired = mojo.isRequiresOnline
            it.isInheritedByDefault = mojo.isInheritByDefault
            it.componentConfigurator = mojo.configurator
            it.isThreadSafe = mojo.isThreadSafe
            mojo.parameters.forEach { parameter -> it.addParameter(toParameter(parameter)) }
        }
    }

    private fun toParameter(parameter: DefaultMavenMojoParameter): Parameter {
        return Parameter().also {
            it.name = parameter.name
            it.type = parameter.type
            it.description = parameter.description
            it.alias = parameter.alias
            it.defaultValue = parameter.defaultValue
            it.expression = parameter.property
            it.isRequired = parameter.isRequired
            it.isEditable = !parameter.isReadonly
        }
    }

    private fun mavenProject(project: Project, sourcesDirs: FileCollection, outputDirectory: File): MavenProject =
        mavenProject(project.group.toString(), project.name, project.version.toString(), sourcesDirs, outputDirectory)

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
            // populate compileSourceRoots in order to extract meta data from JavaDoc
            sourcesDirs.forEach { dir -> it.addCompileSourceRoot(dir.absolutePath) }
        }
    }
}

