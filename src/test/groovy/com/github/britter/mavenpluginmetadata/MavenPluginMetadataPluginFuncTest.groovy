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

class MavenPluginMetadataPluginFuncTest extends Specification {

    @Rule
    TemporaryFolder testProjectDir

    File settingsFile

    File buildFile

    void setup() {
        settingsFile = testProjectDir.newFile("settings.gralde")
        buildFile = testProjectDir.newFile("build.gradle") << """
            plugins {
                id('com.github.britter.maven-plugin-metadata')
            }
        """
    }

    def "can execute task"() {
        when:
        def result = run("greeting")

        then:
        result.output.contains("Hello from plugin 'com.github.britter.maven-plugin-metadata'")
    }

    def run(String... args) {
        def runner = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withArguments(args)
                .withProjectDir(testProjectDir.root)

        runner.build()
    }
}
