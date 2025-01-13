/*
 * Copyright 2022 the GradleX team.
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

import spock.lang.Issue

class KotlinDslCompatibilityTest extends AbstractPluginFuncTest {

    @Issue("https://github.com/gradlex-org/maven-development-plugin/issues/290")
    def "extension can be configured in Kotlin DSL"() {
        given: "A project using Kotlin DSL"
        buildFile.delete()
        file("build.gradle.kts") << """
            plugins {
                id("org.gradlex.maven-plugin-development")
            }
            
            mavenPlugin {
                helpMojoPackage = "com.example.mojo.help"
            }
        """

        expect: "The project can be configured"
        run("tasks")
    }
}
