/*
 * Copyright 2022 Benedikt Ritter
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

package de.benediktritter.maven.plugin.development

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Issue

import java.lang.management.ManagementFactory

class PluginDependenciesFuncTest extends AbstractPluginFuncTest {

    void setup() {
        settingsFile.text = """
        pluginManagement {
            repositories {
                maven {
                    name = 'TestRepo'
                    url = uri("${System.getProperty("test-repo.path")}")
                }
                gradlePluginPortal()
            }
        }
        
        $settingsFile.text
        """
        buildFile.text = """
        plugins {
            id 'com.github.breadmoirai.github-release' version '2.2.12'
        }
        """
        subproject("my-plugin") { project ->
            project.withMavenPluginBuildConfiguration()
            project.javaMojo()
        }
    }

    @Issue("https://github.com/britter/maven-plugin-development/issues/65")
    def "works with plugins that require qdox 1_x"() {
        expect:
        runner(":my-plugin:generateMavenPluginDescriptor").build()
    }

    private def runner(String... args) {
        GradleRunner.create()
                .withGradleVersion(System.getProperty("compat.gradle.version"))
                .forwardOutput()
                .withDebug(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0)
                .withArguments([*args, "-s"])
                .withProjectDir(project.projectDir)
    }
}
