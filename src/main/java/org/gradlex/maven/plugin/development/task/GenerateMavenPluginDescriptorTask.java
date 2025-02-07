/*
 * Copyright the GradleX team.
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

package org.gradlex.maven.plugin.development.task;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifact;
import org.apache.maven.tools.plugin.PluginToolsRequest;
import org.apache.maven.tools.plugin.extractor.ExtractionException;
import org.apache.maven.tools.plugin.generator.GeneratorException;
import org.apache.maven.tools.plugin.generator.PluginDescriptorFilesGenerator;
import org.apache.maven.tools.plugin.scanner.MojoScanner;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradlex.maven.plugin.development.internal.MavenLoggerAdapter;
import org.gradlex.maven.plugin.development.internal.MavenServiceFactory;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

@CacheableTask
public abstract class GenerateMavenPluginDescriptorTask extends AbstractMavenPluginDevelopmentTask {

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getClassesDirs();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getSourcesDirs();

    @Nested
    public abstract ListProperty<UpstreamProjectDescriptor> getUpstreamProjects();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    private final MavenLoggerAdapter loggerAdapter = new MavenLoggerAdapter(getLogger());

    private final MojoScanner scanner = MavenServiceFactory.createMojoScanner(loggerAdapter);

    private final PluginDescriptorFilesGenerator generator = new PluginDescriptorFilesGenerator();

    @TaskAction
    public void generateDescriptor() throws GeneratorException {
        checkArtifactId();

        PluginDescriptor pluginDescriptor = extractPluginDescriptor();
        writeDescriptor(pluginDescriptor);
    }

    private void checkArtifactId() {
        String artifactId = getPluginDescriptor().get().getGav().getArtifactId();
        if (artifactId.startsWith("maven-") && artifactId.endsWith("-plugin")) {
            getLogger().warn("ArtifactIds of the form maven-___-plugin are reserved for plugins of the maven team. Please change the plugin artifactId to the format ___-maven-plugin.");
        }
    }

    private void writeDescriptor(PluginDescriptor pluginDescriptor) throws GeneratorException {
        MavenProject mavenProject = mavenProject(getSourcesDirs(), getOutputDirectory().getAsFile().get());
        generator.execute(getOutputDirectory().dir("META-INF/maven").get().getAsFile(), createPluginToolsRequest(mavenProject, pluginDescriptor));
    }

    private PluginDescriptor extractPluginDescriptor() {
        PluginDescriptor descriptor = createPluginDescriptor();

        getClassesDirs().forEach(classesDir -> {
            MavenProject mavenProject = mavenProject(getSourcesDirs(), classesDir);
            PluginToolsRequest pluginToolsRequest = createPluginToolsRequest(mavenProject, descriptor);
            // process upstream projects in order to scan base classes
            getUpstreamProjects().get().forEach(it -> {
                FileCollection dirs = it.getClassesDirs();
                GAV gav = it.getGav();
                for (File dir : dirs) {
                    DefaultArtifact artifact = new DefaultArtifact(
                            gav.getGroup(), gav.getArtifactId(), gav.getVersion(), "compile", "jar", null, new DefaultArtifactHandler()
                    );
                    artifact.setFile(dir);
                    pluginToolsRequest.getDependencies().add(artifact);
                }
                mavenProject.addProjectReference(mavenProject(gav.getGroup(), gav.getArtifactId(), gav.getVersion(), it.getSourceDirectories(), classesDir));
            });
            populatePluginDescriptor(pluginToolsRequest);
        });

        return descriptor;
    }

    private void populatePluginDescriptor(PluginToolsRequest pluginToolsRequest) {
        try {
            scanner.populatePluginDescriptor(pluginToolsRequest);
        } catch (ExtractionException | InvalidPluginDescriptorException e) {
            throw new RuntimeException(e);
        }
    }

    private MavenProject mavenProject(FileCollection sourcesDirs, File outputDirectory) {
        GAV gav = getPluginDescriptor().get().getGav();
        return mavenProject(
                gav.getGroup(),
                gav.getArtifactId(),
                gav.getVersion(),
                sourcesDirs,
                outputDirectory
        );
    }

    private MavenProject mavenProject(
            String groupId,
            String artifactId,
            String version,
            FileCollection sourcesDirs,
            File outputDirectory
    ) {
        return mavenProject(groupId, artifactId, version, sourcesDirs.getFiles(), outputDirectory);
    }

    private MavenProject mavenProject(
            String groupId,
            String artifactId,
            String version,
            File sourcesDirs,
            File outputDirectory
    ) {
        return mavenProject(groupId, artifactId, version, Collections.singleton(sourcesDirs), outputDirectory);
    }

    private MavenProject mavenProject(
            String groupId,
            String artifactId,
            String version,
            Collection<File> sourcesDirs,
            File outputDirectory
    ) {
        MavenProject project = new MavenProject();

        project.setGroupId(groupId);
        project.setArtifactId(artifactId);
        project.setVersion(version);
        project.setArtifact(new ProjectArtifact(project));
        Build build = new Build();
        build.setOutputDirectory(outputDirectory.getAbsolutePath());
        build.setDirectory(getOutputDirectory().get().getAsFile().getParent());
        project.setBuild(build);
        // populate compileSourceRoots in order to extract metadata from JavaDoc
        sourcesDirs.forEach(dir -> project.addCompileSourceRoot(dir.getAbsolutePath()));

        return project;
    }
}

