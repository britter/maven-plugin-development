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

package org.gradlex.maven.plugin.development

import org.gradle.testkit.runner.TaskOutcome

class BuildCachingFuncTest extends AbstractPluginFuncTest {

    def "supports build caching"() {
        given:
        javaMojo()
        buildFile << "mavenPlugin.helpMojoPackage.set('org.example.help')"

        and:
        run("build", "--build-cache")

        when:
        buildDir.deleteDir()

        and:
        def result = run("build", "--build-cache")

        then:
        result.task(":generateMavenPluginHelpMojoSources").outcome == TaskOutcome.FROM_CACHE
        result.task(":generateMavenPluginDescriptor").outcome == TaskOutcome.FROM_CACHE
    }
}
