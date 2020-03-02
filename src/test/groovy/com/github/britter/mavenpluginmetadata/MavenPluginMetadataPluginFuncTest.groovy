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
        settingsFile = testProjectDir.newFile("settings.gradle") << "rootProject.name=\"maven-touch-plugin\""
        buildFile = testProjectDir.newFile("build.gradle") << """
            plugins {
                id 'java'
                id 'com.github.britter.maven-plugin-metadata'
            }
        """
    }

    def "generates a plugin and help descriptor for mojos in the main source set"() {
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
        testProjectDir.javaMojo("main", "create")
        testProjectDir.groovyMojo()

        when:
        run("generateMavenPluginDescriptor", "-s")

        then:
        def pluginDescriptor = testProjectDir.pluginDescriptor()
        pluginDescriptor.exists()
        def descriptorContents = pluginDescriptor.text
        descriptorContents.contains("<goal>create</goal>")
        descriptorContents.contains("<goal>touch</goal>")

        def helpDescriptor = testProjectDir.helpDescriptor()
        helpDescriptor.exists()
        def helpContents = pluginDescriptor.text
        helpContents.contains("<goal>create</goal>")
        helpContents.contains("<goal>touch</goal>")
    }

    def "generates a plugin descriptor and help descriptor for a different source set"() {
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
        testProjectDir.javaMojo("mojo")

        when:
        run("generateMavenPluginDescriptor", "-s")

        then:
        testProjectDir.pluginDescriptor("mojo").exists()
        testProjectDir.helpDescriptor("mojo").exists()
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
