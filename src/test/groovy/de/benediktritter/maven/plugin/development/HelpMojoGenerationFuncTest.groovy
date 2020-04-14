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

package de.benediktritter.maven.plugin.development

import org.gradle.testkit.runner.TaskOutcome

class HelpMojoGenerationFuncTest extends AbstractPluginFuncTest {

    def "generates HelpMojo"() {
        given:
        javaMojo()
        buildFile << "mavenPlugin.generateHelpMojo.set(true)"

        when:
        def result = run("generateMavenPluginDescriptor")

        then:
        result.task(":generateMavenPluginHelpMojoSources").outcome == TaskOutcome.SUCCESS

        and:
        pluginDescriptor.getMojo("help").implementation == "org.example.HelpMojo"
    }

    def "skips HelpMojo is not configured"() {
        given:
        javaMojo()

        when:
        def result = run("build")

        then:
        result.task(":generateMavenPluginHelpMojoSources").outcome == TaskOutcome.SKIPPED
    }
}
