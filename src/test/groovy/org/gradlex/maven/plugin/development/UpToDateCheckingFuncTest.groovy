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

import org.gradle.testkit.runner.TaskOutcome

class UpToDateCheckingFuncTest extends AbstractPluginFuncTest {

    def "tasks are up to date when nothing has changed"() {
        given:
        javaMojo()
        buildFile << """
            mavenPlugin {
                helpMojoPackage = 'org.example.help'
            }
        """

        when:
        def result = run("build")

        then:
        result.task(":generateMavenPluginHelpMojoSources").outcome == TaskOutcome.SUCCESS
        result.task(":generateMavenPluginDescriptor").outcome == TaskOutcome.SUCCESS

        when:
        result = run(":build")

        then:
        result.task(":generateMavenPluginHelpMojoSources").outcome == TaskOutcome.UP_TO_DATE
        result.task(":generateMavenPluginDescriptor").outcome == TaskOutcome.UP_TO_DATE
    }

    def "is not up to date if mojo dependency changes"() {
        given:
        multiProjectSetup()

        when:
        def result = run("build")

        then:
        result.task(":plugin:generateMavenPluginDescriptor").outcome == TaskOutcome.SUCCESS

        when:
        subproject("touch-mojo") { project ->
            project.file("src/main/java/org/example/TouchMojo.java").replace("LifecyclePhase.PROCESS_SOURCES", "LifecyclePhase.COMPILE")
        }

        and:
        result = run("build")

        then:
        result.task(":plugin:generateMavenPluginDescriptor").outcome == TaskOutcome.SUCCESS
    }

    def "help task correctly cleans up outputs"() {
        given:
        javaMojo()
        buildFile << """
            mavenPlugin {
                helpMojoPackage = 'org.example.help'
            }
        """

        and:
        run("build")

        when:
        buildFile.text = buildFile.text.replace("helpMojoPackage = 'org.example.help'", "helpMojoPackage = 'com.acme.help'")

        and:
        run("build")

        then:
        pluginDescriptor.mojos.size() == 2
    }
}
