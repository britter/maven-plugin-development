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
import org.codehaus.plexus.velocity.DefaultVelocityComponent
import org.codehaus.plexus.velocity.VelocityComponent
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.util.*

@CacheableTask
abstract class GenerateHelpMojoSourcesTask : AbstractMavenPluginDevelopmentTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    // Implicit output of PluginHelpGenerator#execute
    @get:OutputFile
    abstract val helpPropertiesFile: RegularFileProperty

    @get:Input
    abstract val helpMojoPackage: Property<String>



    @TaskAction
    fun generateHelpMojo() {
        val loggerAdapter = MavenLoggerAdapter(logger)

        val generator = PluginHelpGenerator().also { gen ->
            gen.enableLogging(loggerAdapter)
            gen.velocityComponent = createVelocityComponent(loggerAdapter)
        }
        generator.setHelpPackageName(helpMojoPackage.get())
        generator.execute(
            outputDirectory.get().asFile,
            createPluginToolsRequest(mavenProject(), createPluginDescriptor())
        )
    }

    private fun mavenProject(): MavenProject {
        val propertiesDirectory = helpPropertiesFile.get().asFile.parentFile
        propertiesDirectory.mkdirs()
        return projectInfo.map { project ->
            MavenProject().also {
                it.groupId = project.group
                it.artifactId = project.name
                it.version = project.version
                it.artifact = ProjectArtifact(it)
                it.build = Build().also { b -> b.directory = propertiesDirectory.absolutePath }
            }
        }.get()
    }

    private fun createVelocityComponent(loggerAdapter: MavenLoggerAdapter): VelocityComponent {
        val velocityComponent = DefaultVelocityComponent()
        velocityComponent.enableLogging(loggerAdapter)
        // initialization as defined by org.codehaus.plexus:plexus-velocity:1.1.8:META-INF/plexus/components.xml
        val velocityProperties = Properties().also {
            it["resource.loader"] = "classpath,site"
            it["classpath.resource.loader.class"] = "org.codehaus.plexus.velocity.ContextClassLoaderResourceLoader"
            it["site.resource.loader.class"] = "org.codehaus.plexus.velocity.SiteResourceLoader"
            it["runtime.log.invalid.references"] = "false"
            it["velocimacro.messages.on"] = "false"
            it["resource.manager.logwhenfound"] = "false"
        }
        val javaClass = velocityComponent.javaClass
        val propertiesField = javaClass.getDeclaredField("properties")
        propertiesField.isAccessible = true
        propertiesField.set(velocityComponent, velocityProperties)
        // fix for broken debugging due to reflection happening inside Velocity (RuntimeInstance#initializeResourceManager())
        val currentContextClassLoader = Thread.currentThread().contextClassLoader
        try {
            Thread.currentThread().contextClassLoader = javaClass.classLoader
            velocityComponent.initialize()
        } finally {
            Thread.currentThread().contextClassLoader = currentContextClassLoader
        }
        return velocityComponent
    }

}
