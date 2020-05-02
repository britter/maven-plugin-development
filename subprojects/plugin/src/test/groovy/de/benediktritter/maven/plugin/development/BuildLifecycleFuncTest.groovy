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
import spock.lang.Unroll

class BuildLifecycleFuncTest extends AbstractPluginFuncTest {

    def setup() {
        javaMojo()
    }

    @Unroll
    def "task is executed when #task lifecycle task is executed"() {
        when:
        def result = run(task)

        then:
        result.task(":generateMavenPluginDescriptor").outcome == TaskOutcome.SUCCESS

        and:
        pluginJar.contains(pluginDescriptor)
        pluginJar.contains(helpDescriptor)

        where:
        task << ["jar", "build"]
    }

    def "works without applying other plugins"() {
        when:
        buildFile.text = buildFile.text.replace("id 'java'", "")

        then:
        run("build")
    }

    def "tasks are documented"() {
        when:
        def result = run("tasks")

        then:
        result.output.contains("Maven Plugin Development tasks")
        result.output.contains("generateMavenPluginDescriptor")
        result.output.contains("generateMavenPluginHelpMojoSources")
    }
}
