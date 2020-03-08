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

package com.github.britter.maven.plugin.development.fixtures

import org.apache.commons.lang3.StringUtils
import org.junit.rules.ExternalResource
import org.junit.rules.TemporaryFolder

class Workspace extends ExternalResource {

    private TemporaryFolder workspaceFolder = new TemporaryFolder()

    @Override
    void before() {
        workspaceFolder.create()
    }

    @Override
    void after() {
        workspaceFolder.delete()
    }

    File getRoot() {
        workspaceFolder.root
    }

    File dir(String path) {
        def dir
        if (path.startsWith("/")) {
            dir = new File(path)
        } else {
            dir = new File(root, path)
        }
        dir.mkdirs()
        dir
    }

    File file(String path) {
        if (path.contains("/")) {
            def parentDir = dir(StringUtils.substringBeforeLast(path, '/'))
            new File(parentDir, StringUtils.substringAfterLast(path, '/'))
        } else {
            new File(root, path)
        }
    }

    File getSettingsFile() {
        file("settings.gradle")
    }

    File getBuildFile() {
        file("build.gradle")
    }

    def javaMojo(String sourceSetName = "main", String mojoName = "touch") {
        def className = "${mojoName.capitalize()}Mojo"
        file("src/$sourceSetName/java/org/example/${className}.java") << """
            package org.example;
            import java.io.*;
            import org.apache.maven.plugin.AbstractMojo;
            import org.apache.maven.plugin.MojoExecutionException;
            import org.apache.maven.plugins.annotations.*;
            @Mojo(
                name = "$mojoName",
                defaultPhase = LifecyclePhase.PROCESS_SOURCES
            )
            public class $className extends AbstractMojo {

                /**
                 * The output directory to put the file into.
                 */
                @Parameter(defaultValue = "\${project.build.outputDirectory}", property = "myMojo.outputDirectory")
                private File outputDirectory;

                /**
                 * The name of the file to put into the output directory.
                 */
                @Parameter(defaultValue = "touch.txt", property = "myMojo.fileName")
                private File fileName;

                @Override
                public void execute() throws MojoExecutionException {
                    outputDirectory.mkdirs();
                    File touch = new File(outputDirectory, "touch.txt");
                    try(FileWriter writer = new FileWriter(touch)) {
                        writer.write("");
                    } catch (IOException e) {
                        throw new MojoExecutionException("Error creating file " + touch, e);
                    }
                }
            }
        """
    }

    def groovyMojo() {
        file("src/main/groovy/org/example/TouchMojo.groovy") << '''
            package org.example;
            import java.io.*;
            import org.apache.maven.plugin.AbstractMojo;
            import org.apache.maven.plugin.MojoExecutionException;
            import org.apache.maven.plugins.annotations.*;
            @Mojo(
                name = "touch",
                defaultPhase = LifecyclePhase.PROCESS_SOURCES
            )
            class TouchMojo extends AbstractMojo {

                /**
                 * The output directory to put the file into.
                 */
                @Parameter(defaultValue = '${project.build.outputDirectory}', property = "myMojo.outputDirectory")
                private File outputDirectory

                /**
                 * The name of the file to put into the output directory.
                 */
                @Parameter(defaultValue = "touch.txt", property = "myMojo.fileName")
                private File fileName

                @Override
                void execute() throws MojoExecutionException {
                    outputDirectory.mkdirs()
                    new File(outputDirectory, "touch.txt") << ""
                }
            }
        '''
    }

    DescriptorFile getPluginDescriptor(String sourceSetName = "main") {
        new DescriptorFile(file("build/resources/$sourceSetName/META-INF/maven/plugin.xml"))
    }

    DescriptorFile getHelpDescriptor(String sourceSetName = "main") {
        new DescriptorFile(file("build/resources/$sourceSetName/META-INF/maven/org.example/touch-maven-plugin/plugin-help.xml"))
    }
}
