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

package de.benediktritter.maven.plugin.development.task;

import de.benediktritter.maven.plugin.development.internal.MavenLoggerAdapter;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifact;
import org.apache.maven.tools.plugin.generator.GeneratorException;
import org.apache.maven.tools.plugin.generator.PluginHelpGenerator;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.codehaus.plexus.velocity.internal.DefaultVelocityComponent;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;

@CacheableTask
public abstract class GenerateHelpMojoSourcesTask extends AbstractMavenPluginDevelopmentTask {

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    // Implicit output of PluginHelpGenerator#execute
    @OutputFile
    public abstract RegularFileProperty getHelpPropertiesFile();

    @Input
    public abstract Property<String> getHelpMojoPackage();

    private final MavenLoggerAdapter loggerAdapter = new MavenLoggerAdapter(getLogger());

    @TaskAction
    public void generateHelpMojo() throws GeneratorException {
        PluginHelpGenerator generator = new PluginHelpGenerator();
        generator.enableLogging(loggerAdapter);
        generator.setVelocityComponent(createVelocityComponent());
        generator.setHelpPackageName(getHelpMojoPackage().get());
        generator.setMavenProject(mavenProject());
        generator.execute(getOutputDirectory().get().getAsFile());
    }

    private MavenProject mavenProject() {
        File propertiesDirectory = getHelpPropertiesFile().get().getAsFile().getParentFile();
        propertiesDirectory.mkdirs();

        MavenProject project = new MavenProject();
        project.setGroupId(getPluginDescriptor().get().getGroupId());
        project.setArtifactId(getPluginDescriptor().get().getArtifactId());
        project.setVersion(getPluginDescriptor().get().getVersion());
        project.setArtifact(new ProjectArtifact(project));
        Build build = new Build();
        build.setDirectory(propertiesDirectory.getAbsolutePath());
        project.setBuild(build);

        return project;
    }

    private VelocityComponent createVelocityComponent() {
        // fix for broken debugging due to reflection happening inside Velocity (RuntimeInstance#initializeResourceManager())
        ClassLoader currentContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return new DefaultVelocityComponent(null);
        } finally {
            Thread.currentThread().setContextClassLoader(currentContextClassLoader);
        }
    }
}