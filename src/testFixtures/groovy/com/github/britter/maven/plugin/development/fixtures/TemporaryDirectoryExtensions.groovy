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

import org.junit.rules.TemporaryFolder

class TemporaryDirectoryExtensions {

    static def javaMojo(TemporaryFolder self, String sourceSetName = "main", String mojoName = "touch") {
        File dir = new File(self.root, "src/$sourceSetName/java/org/example")
        dir.mkdirs()
        def className = "${mojoName.capitalize()}Mojo"
        new File(dir, "${className}.java") << """
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

    static def groovyMojo(TemporaryFolder self) {
        File dir = new File(self.root, "src/main/groovy/org/example")
        dir.mkdirs()
        new File(dir, "TouchMojo.groovy") << '''
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

    static File pluginDescriptor(TemporaryFolder self, String sourceSetName = "main") {
        new File(self.root, "build/resources/$sourceSetName/META-INF/maven/plugin.xml")
    }

    static File helpDescriptor(TemporaryFolder self, String sourceSetName = "main") {
        new File(self.root, "build/resources/$sourceSetName/META-INF/maven/org.example/touch-maven-plugin/plugin-help.xml")
    }
}
