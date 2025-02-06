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

package org.gradlex.maven.plugin.development

import org.gradle.testkit.runner.GradleRunner
import org.gradlex.maven.plugin.development.fixtures.TestRootProject
import org.junit.Rule
import spock.lang.Specification

import java.lang.management.ManagementFactory

abstract class AbstractPluginFuncTest extends Specification {

    @Rule
    @Delegate
    TestRootProject project

    void setup() {
        settingsFile << "rootProject.name=\"touch-maven-plugin\""
        withMavenPluginBuildConfiguration()
    }

    def run(String... args) {
        runner(args).build()
    }

    def runAndFail(String... args) {
        runner(args).buildAndFail()
    }

    def runner(String... args) {
        GradleRunner.create()
                .forwardOutput()
                .withDebug(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0)
                .withPluginClasspath()
                .withArguments([*args, "-s"])
                .withProjectDir(project.projectDir)
    }
}
