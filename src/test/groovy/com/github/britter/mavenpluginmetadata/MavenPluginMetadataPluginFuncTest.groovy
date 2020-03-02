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
        settingsFile = testProjectDir.newFile("settings.gradle") << "rootProject.name=\"touch-maven-plugin\""
        buildFile = testProjectDir.newFile("build.gradle") << """
            plugins {
                id 'java'
                id 'com.github.britter.maven-plugin-metadata'
            }

            group "org.example"
            description "A maven plugin with a mojo that can touch it!"
            version "1.0.0"

            repositories {
                mavenCentral()
            }
            dependencies {
                implementation 'org.apache.maven:maven-plugin-api:3.6.3'
                implementation 'org.apache.maven.plugin-tools:maven-plugin-annotations:3.6.0'
            }
        """
    }

    def "adds project metadata"() {
        given:
        testProjectDir.javaMojo()

        when:
        run("generateMavenPluginDescriptor")

        then:
        assertDescriptorContents(testProjectDir.pluginDescriptor(),
                "touch-maven-plugin",
                "A maven plugin with a mojo that can touch it!",
                "org.example",
                "touch-maven-plugin",
                "1.0.0",
                "touch"
        )
        assertDescriptorContents(testProjectDir.helpDescriptor(),
                "touch-maven-plugin",
                "A maven plugin with a mojo that can touch it!",
                "org.example",
                "touch-maven-plugin",
                "1.0.0",
                "touch"
        )
    }

    def "adds customized metadata"() {
        given:
        buildFile << """
            mavenPluginMetadata {
                pluginDescriptor {
                    name.set("custom-name")
                    description.set("custom description")
                    groupId.set("com.acme")
                    artifactId.set("custom-artifact-id")
                    version.set("2.0-custom")
                    goalPrefix.set("custom-prefix")
                }
            }
        """
        testProjectDir.javaMojo()

        when:
        run("generateMavenPluginDescriptor")

        then:
        assertDescriptorContents(testProjectDir.pluginDescriptor(),
            "custom-name",
            "custom description",
            "com.acme",
            "custom-artifact-id",
            "2.0-custom",
            "custom-prefix"
        )
        assertDescriptorContents(testProjectDir.helpDescriptor(),
            "custom-name",
            "custom description",
            "com.acme",
            "custom-artifact-id",
            "2.0-custom",
            "custom-prefix"
        )
    }

    def "generates a plugin and help descriptor for mojos in the main source set"() {
        given:
        buildFile << """
            apply plugin: 'groovy'
            dependencies {
                implementation localGroovy()
            }
        """
        testProjectDir.javaMojo("main", "create")
        testProjectDir.groovyMojo()

        when:
        run("generateMavenPluginDescriptor")

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
        run("generateMavenPluginDescriptor")

        then:
        testProjectDir.pluginDescriptor("mojo").exists()
        testProjectDir.helpDescriptor("mojo").exists()
    }

    void assertDescriptorContents(
            File descriptorFile,
            String name,
            String description,
            String groupId,
            String artifactId,
            String version,
            String goalPrefix) {
        descriptorFile.exists()
        def descriptorContents = descriptorFile.text
        descriptorContents.contains("<name>$name</name>")
        descriptorContents.contains("<description>$description</description>")
        descriptorContents.contains("<groupId>$groupId</groupId>")
        descriptorContents.contains("<artifactId>$artifactId</artifactId>")
        descriptorContents.contains("<version>$version</version>")
        descriptorContents.contains("<goalPrefix>$goalPrefix</goalPrefix>")
    }

    def run(String... args) {
        def runner = GradleRunner.create()
                .forwardOutput()
                .withDebug(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0)
                .withPluginClasspath()
                .withArguments([*args, "-s"])
                .withProjectDir(testProjectDir.root)

        runner.build()
    }
}
