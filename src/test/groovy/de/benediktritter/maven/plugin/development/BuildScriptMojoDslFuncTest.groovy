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

import de.benediktritter.maven.plugin.development.fixtures.DescriptorFile
import org.gradle.testkit.runner.TaskOutcome

class BuildScriptMojoDslFuncTest extends AbstractPluginFuncTest {

    def "provides DSL for defining mojos in the build script"() {
        given:
        buildFile << """
            mavenPlugin {
                mojos {
                    touch {
                        description = "This is an awesome mojo!"
                        implementation = "de.benediktritter.maven.SomeMojo"
                        language = "kotlin"
                        defaultPhase = org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_RESOURCES
                        requiresDependencyResolution = org.apache.maven.plugins.annotations.ResolutionScope.COMPILE
                        requiresDependencyCollection = org.apache.maven.plugins.annotations.ResolutionScope.TEST
                        instantiationStrategy = org.apache.maven.plugins.annotations.InstantiationStrategy.SINGLETON
                        executionStrategy = de.benediktritter.maven.plugin.development.MavenMojo.ExecutionStrategy.ALWAYS
                        requiresProject = false
                        requiresReports = true
                        aggregator = true
                        requiresDirectInvocation = true
                        requiresOnline = true
                        inheritByDefault = false
                        configurator = "de.benediktritter.maven.SomeConfigurer"
                        threadSafe = true
                    }
                }
            }
        """

        when:
        run("generateMavenPluginDescriptor")

        then:
        pluginDescriptor.hasGoal("touch")
        def mojo = pluginDescriptor.getMojo("touch")
        mojo.description == "This is an awesome mojo!"
        mojo.implementation == "de.benediktritter.maven.SomeMojo"
        mojo.language == "kotlin"
        mojo.phase == "process-resources"
        mojo.requiresDependencyResolution == "compile"
        mojo.requiresDependencyCollection == "test"
        mojo.instantiationStrategy == "singleton"
        mojo.executionStrategy == "always"
        !mojo.requiresProject
        mojo.requiresReports
        mojo.aggregator
        mojo.requiresDirectInvocation
        mojo.requiresOnline
        !mojo.inheritedByDefault
        mojo.configurator == "de.benediktritter.maven.SomeConfigurer"
        mojo.threadSafe
    }

    def "throws error when implementation is missing"() {
        given:
        buildFile << """
            mavenPlugin {
                mojos {
                    touch {
                        description = "This is an awesome mojo!"
                    }
                }
            }
        """

        when:
        def result = runAndFail("generateMavenPluginDescriptor")

        then:
        result.task(":generateMavenPluginDescriptor").outcome == TaskOutcome.FAILED
        result.output.contains("implementation")
        !result.output.contains("configurator")
    }

    def "provides DSL for defining mojo parameters"() {
        given:
        buildFile << """
            mavenPlugin {
                mojos {
                    touch {
                        implementation = "de.benediktritter.maven.SomeMojo"
                        parameters {
                            parameter("outputDirectory", "java.io.File") {
                                it.description = "The output directory for this mojo"
                                it.alias = "out"
                                it.property = "touch.outputDirectory"
                                it.defaultValue = '\${some.dollar.expression}'
                                it.required = true
                                it.readonly = true
                            }
                            parameter("outputFile", File)
                        }
                    }
                }
            }
        """

        when:
        run("generateMavenPluginDescriptor")

        then:
        def mojo = pluginDescriptor.getMojo("touch")
        mojo.parameters.size() == 2
        def outputDir = mojo.getParameter("outputDirectory")
        outputDir.type == File
        outputDir.description == "The output directory for this mojo"
        outputDir.alias == "out"
        outputDir.required
        !outputDir.editable

        def outputFile = mojo.getParameter("outputFile")
        outputFile.type == File
        outputFile.description == ""
        outputFile.alias == ""
        !outputFile.required
        outputFile.editable
    }
}
