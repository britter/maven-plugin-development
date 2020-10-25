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
import org.apache.maven.model.Build
import org.apache.maven.plugin.descriptor.MojoDescriptor
import org.apache.maven.plugin.descriptor.Parameter
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.project.MavenProject
import org.apache.maven.project.artifact.ProjectArtifact
import org.apache.maven.tools.plugin.ExtendedMojoDescriptor
import org.apache.maven.tools.plugin.generator.PluginDescriptorGenerator
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the
import java.io.File

@CacheableTask
abstract class GenerateMavenPluginDescriptorTask : AbstractMavenPluginDevelopmentTask() {

    @get:Input
    abstract val classesDirs: Property<FileCollection>

    @get:Internal
    abstract val javaClassesDir: DirectoryProperty

    @get:Input
    abstract val sourcesDirs: Property<FileCollection>

    @get:Internal
    abstract val mojoDependencies: Property<Configuration>

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
        val mavenProject = mavenProject(sourcesDirs.get(), outputDirectory.asFile.get())
        generator.execute(outputDirectory.dir("META-INF/maven").get().asFile, createPluginToolsRequest(mavenProject, pluginDescriptor))
    }

    private fun extractPluginDescriptor(): PluginDescriptor {
        return createPluginDescriptor().also { pluginDescriptor ->
            classesDirs.get().forEach { classesDir ->
                val mavenProject = mavenProject(sourcesDirs.get(), classesDir)
                val pluginToolsRequest = createPluginToolsRequest(mavenProject, pluginDescriptor)
                scanner.populatePluginDescriptor(pluginToolsRequest)
            }
            getUpstreamProjects().forEach {
                val main = it.the<SourceSetContainer>()["main"]
                main.output.classesDirs.forEach { classesDir ->
                    val mavenProject = mavenProject(main.java.sourceDirectories, classesDir)
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

    private fun mavenProject(sourcesDirs: FileCollection, outputDirectory: File): MavenProject {
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
            sourcesDirs.forEach { dir -> it.addCompileSourceRoot(dir.absolutePath) }
        }
    }

    private fun getUpstreamProjects() = mojoDependencies.get().dependencies
                .filterIsInstance<ProjectDependency>()
                .map { it.dependencyProject }

    @InputFiles @PathSensitive(PathSensitivity.NAME_ONLY)
    fun getMojoProjectSourceDirectories() = getUpstreamProjects().flatMap { it.the<SourceSetContainer>()["main"].java.sourceDirectories }
}

