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

package com.github.britter.mavenpluginmetadata

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.lang.management.ManagementFactory

class MavenPluginMetadataPluginFuncTest extends Specification {

    @Rule
    TemporaryFolder testProjectDir

    File settingsFile

    File buildFile

    void setup() {
        settingsFile = testProjectDir.newFile("settings.gradle") << "rootProject.name=\"touch-mojo\""
        buildFile = testProjectDir.newFile("build.gradle") << """
            plugins {
                id 'java'
                id 'com.github.britter.maven-plugin-metadata'
            }
        """
    }

    def "generates a plugin descriptor and a help descriptor for a Java mojo"() {
        given:
        buildFile << """
            group "org.example"
            repositories {
                mavenCentral()
            }
            dependencies {
                implementation 'org.apache.maven:maven-plugin-api:3.6.3'
                implementation 'org.apache.maven.plugin-tools:maven-plugin-annotations:3.6.0'
            }
        """
        def dir = testProjectDir.newFolder("src", "main", "java", "org", "example")
        new File(dir, "TouchMojo.java") << '''
            package org.example;
            import java.io.*;
            import org.apache.maven.plugin.AbstractMojo;
            import org.apache.maven.plugin.MojoExecutionException;
            import org.apache.maven.plugins.annotations.*;
            @Mojo(
                name = "touch",
                defaultPhase = LifecyclePhase.PROCESS_SOURCES
            )
            public class TouchMojo extends AbstractMojo {

                /**
                 * The output directory to put the file into.
                 */
                @Parameter(defaultValue = "${project.build.outputDirectory}", property = "myMojo.outputDirectory")
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
        '''

        when:
        run("generateMavenPluginDescriptor", "-s")

        then:
        def outputDir = new File(testProjectDir.root, "build/resources/main/META-INF/maven")
        new File(outputDir, "plugin.xml").exists()
        new File(outputDir, "org.example/touch-mojo/plugin-help.xml").exists()
    }

    def "generates a plugin descriptor and a help descriptor for a groovy mojo"() {
        given:
        buildFile << """
            apply plugin: 'groovy'
            group "org.example"
            repositories {
                mavenCentral()
            }
            dependencies {
                implementation localGroovy()
                implementation 'org.apache.maven:maven-plugin-api:3.6.3'
                implementation 'org.apache.maven.plugin-tools:maven-plugin-annotations:3.6.0'
            }
        """
        def dir = testProjectDir.newFolder("src", "main", "groovy", "org", "example")
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

        when:
        run("generateMavenPluginDescriptor", "-s")

        then:
        def outputDir = new File(testProjectDir.root, "build/resources/main/META-INF/maven")
        new File(outputDir, "plugin.xml").exists()
        new File(outputDir, "org.example/touch-mojo/plugin-help.xml").exists()
    }

    def "generates a plugin descriptor and a help descriptor for a different SourceSet"() {
        given:
        buildFile << """
            group "org.example"
            repositories {
                mavenCentral()
            }
            def mojoSourceSet = sourceSets.create('mojo')
            mavenPluginMetadata {
                sourceSet = mojoSourceSet
            }
            dependencies {
                mojoImplementation 'org.apache.maven:maven-plugin-api:3.6.3'
                mojoImplementation 'org.apache.maven.plugin-tools:maven-plugin-annotations:3.6.0'
            }
        """
        def dir = testProjectDir.newFolder("src", "mojo", "java", "org", "example")
        new File(dir, "TouchMojo.java") << '''
            package org.example;
            import java.io.*;
            import org.apache.maven.plugin.AbstractMojo;
            import org.apache.maven.plugin.MojoExecutionException;
            import org.apache.maven.plugins.annotations.*;
            @Mojo(
                name = "touch",
                defaultPhase = LifecyclePhase.PROCESS_SOURCES
            )
            public class TouchMojo extends AbstractMojo {
    
                /**
                 * The output directory to put the file into.
                 */
                @Parameter(defaultValue = "${project.build.outputDirectory}", property = "myMojo.outputDirectory")
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
        '''

        when:
        run("generateMavenPluginDescriptor", "-s")

        then:
        def outputDir = new File(testProjectDir.root, "build/resources/mojo/META-INF/maven")
        new File(outputDir, "plugin.xml").exists()
        new File(outputDir, "org.example/touch-mojo/plugin-help.xml").exists()
    }

    def run(String... args) {
        def runner = GradleRunner.create()
                .forwardOutput()
                .withDebug(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0)
                .withPluginClasspath()
                .withArguments(args)
                .withProjectDir(testProjectDir.root)

        runner.build()
    }
}
