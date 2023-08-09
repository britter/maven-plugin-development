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

import groovy.transform.Memoized
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Issue
import spock.lang.Requires

@Requires(value = { configurationSupported() }, reason = "Configuration Cache was added in Gradle 6.6")
class ConfigurationCacheFuncTest extends AbstractPluginFuncTest {

    @Issue("https://github.com/britter/maven-plugin-development/issues/8")
    def "tasks are configuration cache compatible"() {
        given:
        javaMojo()

        when:
        def result = run("build", "--configuration-cache")

        then:
        result.task(":generateMavenPluginHelpMojoSources").outcome == TaskOutcome.SUCCESS
        result.task(":generateMavenPluginDescriptor").outcome == TaskOutcome.SUCCESS
    }

    @Memoized
    static boolean configurationSupported() {
        def gradleVersion = (System.getProperty("compat.gradle.version") =~ /^(?<major>\d+)\.(?<minor>\d+)/)
        if (gradleVersion.find()) {
            def major = gradleVersion.group("major") as int
            def minor = gradleVersion.group("minor") as int

            // Configuration cache was introduced in 6.6
            return major > 7 || (major == 6 && minor >= 6)
        }
        return false
    }
}
