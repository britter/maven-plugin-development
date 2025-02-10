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

package org.gradlex.maven.plugin.development.issues

import org.gradlex.maven.plugin.development.AbstractPluginFuncTest
import spock.lang.Issue

@Issue("https://github.com/gradlex-org/maven-plugin-development/issues/294")
class Issue294FuncTest extends AbstractPluginFuncTest {

    def "works in presence of #plugin plugin"() {
        given:
        buildFile.text = buildFile.text.replace("id 'java'", id)
        buildFile << """
            mavenPlugin.setHelpMojoPackage("com.example.help")
        """

        expect:
        run("build")

        where:
        plugin   | id
        'groovy' | "id 'groovy'"
        'kotlin' | "id 'org.jetbrains.kotlin.jvm' version '2.1.10'"
    }
}
