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

package org.gradlex.maven.plugin.development.fixtures

import org.junit.rules.ExternalResource
import org.junit.rules.TemporaryFolder

class TestRootProject extends ExternalResource implements TestProject {

    private TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Lazy DescriptorFile pluginDescriptor = pluginDescriptor()

    @Lazy DescriptorFile helpDescriptor = helpDescriptor()

    @Override
    void before() {
        temporaryFolder.create()
    }

    @Override
    void after() {
        temporaryFolder.delete()
    }

    File getProjectDir() {
        temporaryFolder.root
    }

    TestProject subproject(String projectName, @DelegatesTo(TestProject) Closure<TestProject> configureProject) {
        if (!settingsFile.text.contains(projectName)) {
            settingsFile << """
                include '$projectName'
            """
        }
        TestSubproject sub = subproject(projectName)
        configureProject.delegate = sub
        configureProject.call(sub)
        sub
    }

    TestSubproject subproject(String projectName) {
        new TestSubproject(dir(projectName))
    }

    def multiProjectSetup() {
        settingsFile.text = "rootProject.name = 'root-project'"
        buildFile.text = ""
        subproject("touch-mojo") { project ->
            project.withMavenPluginBuildConfiguration(false)
            project.javaMojo()
        }
        subproject("plugin") { project ->
            project.buildFile << """
                plugins {
                    id 'java'
                    id 'org.gradlex.maven-plugin-development'
                }
                repositories {
                    mavenCentral()
                }
                dependencies {
                    implementation project(":touch-mojo") 
                }
            """
        }
    }

    def multiProjectKotlinSetup() {
        settingsFile.text = "rootProject.name = 'root-project'"
        buildFile.text = ""
        subproject("kotlin-lib") { project ->
            project.buildFile << """
                plugins {
                    id 'org.jetbrains.kotlin.jvm' version '2.1.0'
                    id 'java-library'
                }
    
                group = "org.example"
                version = "1.0.0"
                
                repositories {
                    mavenCentral()
                }
            """
            project.kotlinClass()
        }
        subproject("plugin") { project ->
            project.buildFile << """
                plugins {
                    id 'java'
                    id 'org.gradlex.maven-plugin-development'
                }
                repositories {
                    mavenCentral()
                }
                dependencies {
                    implementation project(":kotlin-lib") 
                }
            """
        }
    }
}
