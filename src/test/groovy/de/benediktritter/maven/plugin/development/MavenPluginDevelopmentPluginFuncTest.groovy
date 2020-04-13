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
import spock.lang.Unroll

class MavenPluginDevelopmentPluginFuncTest extends AbstractPluginFuncTest {

    def "adds project metadata"() {
        given:
        javaMojo()

        when:
        run("generateMavenPluginDescriptor")

        then:
        pluginDescriptor.name == "touch-maven-plugin"
        pluginDescriptor.description == "A maven plugin with a mojo that can touch it!"
        pluginDescriptor.groupId == "org.example"
        pluginDescriptor.artifactId == "touch-maven-plugin"
        pluginDescriptor.version == "1.0.0"
        pluginDescriptor.goalPrefix == "touch"

        and:
        helpDescriptor.name == "touch-maven-plugin"
        helpDescriptor.description == "A maven plugin with a mojo that can touch it!"
        helpDescriptor.groupId == "org.example"
        helpDescriptor.artifactId == "touch-maven-plugin"
        helpDescriptor.version == "1.0.0"
        helpDescriptor.goalPrefix == "touch"
    }

    def "adds customized metadata"() {
        given:
        buildFile << """
            mavenPlugin {
                name.set("custom-name")
                description.set("custom description")
                groupId.set("com.acme")
                artifactId.set("custom-artifact-id")
                version.set("2.0-custom")
                goalPrefix.set("custom-prefix")
            }
        """
        javaMojo()

        when:
        run("generateMavenPluginDescriptor")

        then:
        pluginDescriptor.name == "custom-name"
        pluginDescriptor.description == "custom description"
        pluginDescriptor.groupId == "com.acme"
        pluginDescriptor.artifactId == "custom-artifact-id"
        pluginDescriptor.version == "2.0-custom"
        pluginDescriptor.goalPrefix == "custom-prefix"

        and:
        helpDescriptor.name == "custom-name"
        helpDescriptor.description == "custom description"
        helpDescriptor.groupId == "com.acme"
        helpDescriptor.artifactId == "custom-artifact-id"
        helpDescriptor.version == "2.0-custom"
        helpDescriptor.goalPrefix == "custom-prefix"
    }

    def "description is optional"() {
        given:
        javaMojo()
        buildFile << "description = null"

        when:
        run("generateMavenPluginDescriptor")

        then:
        pluginDescriptor.description == ""
    }

    def "extracts metadata from JavaDoc"() {
        given:
        javaMojo()

        when:
        run("generateMavenPluginDescriptor")

        then:
        def mojo = pluginDescriptor.getMojo("touch")
        mojo.description == "A mojo written in Java that touches a file."
        mojo.parameters.size() == 2
        mojo.parameters.contains(new DescriptorFile.ParameterDeclaration("fileName", File, false, true, "The name of the file to put into the output directory."))
        mojo.parameters.contains(new DescriptorFile.ParameterDeclaration("outputDirectory", File, false, true, "The output directory to put the file into."))
    }

    def "warns against invalid coordinates"() {
        given:
        buildFile << """
            mavenPlugin {
                artifactId.set("maven-touch-plugin")
            }
        """
        javaMojo()

        when:
        def result = run("generateMavenPluginDescriptor")

        then:
        result.output.contains("ArtifactIds of the form maven-___-plugin are reserved for plugins of the maven team. Please change the plugin artifactId to the format ___-maven-plugin.")
    }

    def "works even if weird enums are present"() {
        given:
        file("src/main/java/TypedEnum.java") << """
        import java.util.List;
        public enum TypedEnum {
            ENUM_VALUE {
                private <T> List<T> genericMethod(List<T> list) {
                    return list;
                }
            }
        }
        """

        expect:
        run("generateMavenPluginDescriptor")
    }

    def "generates a plugin and help descriptor for mojos in the main source set"() {
        given:
        buildFile << """
            apply plugin: 'groovy'
            dependencies {
                implementation localGroovy()
            }
        """
        javaMojo("main", "create")
        groovyMojo()

        when:
        run("generateMavenPluginDescriptor")

        then:
        pluginDescriptor.hasGoal("create")
        pluginDescriptor.hasGoal("touch")

        and:
        helpDescriptor.hasGoal("create")
        helpDescriptor.hasGoal("touch")
    }

    def "generates a plugin and help descriptor from JavaDoc"() {
        given:
        javaDocMojo()

        when:
        run("generateMavenPluginDescriptor")

        then:
        pluginDescriptor.hasGoal("touch")

        and:
        helpDescriptor.hasGoal("touch")
    }

    def "generates a plugin descriptor and help descriptor for a different source set"() {
        given:
        buildFile << """
            def mojoSourceSet = sourceSets.create('mojo')
            mavenPlugin {
                pluginSourceSet = mojoSourceSet
            }
            dependencies {
                mojoImplementation 'org.apache.maven:maven-plugin-api:3.6.3'
                mojoImplementation 'org.apache.maven.plugin-tools:maven-plugin-annotations:3.6.0'
            }
        """
        javaMojo("mojo")

        when:
        run("generateMavenPluginDescriptor")

        then:
        pluginDescriptor()
        helpDescriptor()
    }

    def "adds direct and transitive runtime dependencies to plugin descriptor"() {
        given:
        buildFile << """
            apply plugin: 'java-library'
            dependencies {
                api 'org.apache.commons:commons-lang3:3.6'
                implementation 'com.google.guava:guava:28.0-jre'
                compileOnly 'commons-io:commons-io:2.6'
                runtimeOnly 'org.apache.commons:commons-math3:3.6.1'
                testImplementation 'junit:junit:4.12'
            }
        """
        javaMojo()

        when:
        run("generateMavenPluginDescriptor")

        then:
        pluginDescriptor.hasDependency('org.apache.commons:commons-lang3:3.8.1') // selected by conflict resolution
        pluginDescriptor.hasDependency('com.google.guava:guava:28.0-jre')
        pluginDescriptor.hasDependency('com.google.guava:failureaccess:1.0.1') // transitive guava dependency
        pluginDescriptor.hasDependency('org.apache.commons:commons-math3:3.6.1')
        !pluginDescriptor.hasDependency('commons-io:commons-io:2.6')
        !pluginDescriptor.hasDependency('junit:junit:4.12')

        and:
        helpDescriptor.hasNoDependencies()
    }

    def "finds mojos in project dependencies"() {
        given:
        settingsFile.text = "rootProject.name = 'root-project'"
        buildFile.text = ""
        subproject("touch-mojo") { project ->
            project.withMavenPluginBuildConfiguration(false)
            project.javaMojo()
        }
        subproject("create-mojo") { project ->
            project.withMavenPluginBuildConfiguration(false)
            project.javaMojo("main", "create")
        }
        def pluginProject = subproject("plugin") { project ->
            project.buildFile << """
                plugins {
                    id 'java'
                    id 'de.benediktritter.maven-plugin-development'
                }
                repositories {
                    mavenCentral()
                }
                dependencies {
                    mojo project(":touch-mojo") 
                    implementation project(":create-mojo") 
                }
            """
        }

        when:
        run(":plugin:build")

        then:
        pluginProject.pluginDescriptor.hasGoal("touch")
        !pluginProject.pluginDescriptor.hasGoal("create")
        def mojo = pluginProject.pluginDescriptor.getMojo("touch")
        mojo.description == "A mojo written in Java that touches a file."
        mojo.parameters.size() == 2
        mojo.parameters.contains(new DescriptorFile.ParameterDeclaration("fileName", File, false, true, "The name of the file to put into the output directory."))
        mojo.parameters.contains(new DescriptorFile.ParameterDeclaration("outputDirectory", File, false, true, "The output directory to put the file into."))
    }

    def "provides control over plugin dependencies"() {
        given:
        javaMojo()
        buildFile << """
            configurations {
              deps
            }
            dependencies {
              deps "org.apache.commons:commons-lang3:3.9"
              implementation "com.google.guava:guava:28.0-jre"
            }
            mavenPlugin {
              dependencies = configurations.deps
            }
        """

        when:
        run(":generateMavenPluginDescriptor")

        then:
        pluginDescriptor.hasDependency('org.apache.commons:commons-lang3:3.9')
        !pluginDescriptor.hasDependency('com.google.guava:guava:28.0-jre')
    }

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
                        executionStrategy = de.benediktritter.maven.plugin.development.model.ExecutionStrategy.ALWAYS
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

    @Unroll
    def "task is executed when #task lifecycle task is executed"() {
        given:
        javaMojo()

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
