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
import org.apache.maven.model.Build
import org.apache.maven.project.MavenProject
import org.apache.maven.project.artifact.ProjectArtifact
import org.apache.maven.tools.plugin.generator.PluginHelpGenerator
import org.codehaus.plexus.velocity.VelocityComponent
import org.codehaus.plexus.velocity.internal.DefaultVelocityComponent
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

@CacheableTask
abstract class GenerateHelpMojoSourcesTask : AbstractMavenPluginDevelopmentTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    // Implicit output of PluginHelpGenerator#execute
    @get:OutputFile
    abstract val helpPropertiesFile: RegularFileProperty

    @get:Input
    abstract val helpMojoPackage: Property<String>

    private val loggerAdapter = MavenLoggerAdapter(logger)

    private val generator = PluginHelpGenerator().also { gen ->
        gen.enableLogging(loggerAdapter)
        gen.setVelocityComponent(createVelocityComponent())
    }

    @TaskAction
    fun generateHelpMojo() {
        generator.setHelpPackageName(helpMojoPackage.get())
        generator.setMavenProject(mavenProject())
        generator.execute(outputDirectory.get().asFile)
    }

    private fun mavenProject(): MavenProject {
        val propertiesDirectory = helpPropertiesFile.get().asFile.parentFile
        propertiesDirectory.mkdirs()
        return MavenProject().also {
            it.groupId = pluginDescriptor.get().groupId
            it.artifactId = pluginDescriptor.get().artifactId
            it.version = pluginDescriptor.get().version
            it.artifact = ProjectArtifact(it)
            it.build = Build().also { b -> b.directory = propertiesDirectory.absolutePath }
        }
    }

    private fun createVelocityComponent(): VelocityComponent {
        // fix for broken debugging due to reflection happening inside Velocity (RuntimeInstance#initializeResourceManager())
        val currentContextClassLoader = Thread.currentThread().contextClassLoader
        return try {
            Thread.currentThread().contextClassLoader = javaClass.classLoader
            DefaultVelocityComponent(null)
        } finally {
            Thread.currentThread().contextClassLoader = currentContextClassLoader
        }
    }
}