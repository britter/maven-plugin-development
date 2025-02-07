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
import org.gradlex.maven.plugin.development.fixtures.DescriptorFile
import org.gradlex.maven.plugin.development.fixtures.TestProject
import spock.lang.Issue

@Issue("https://github.com/gradlex-org/maven-plugin-development/issues/73")
class Issue73FuncTest extends AbstractPluginFuncTest {

    def "extracts properties from base mojos defined outside the project"() {
        given:
        subproject("base-mojo") { project ->
            project.buildFile << """
                plugins {
                    id 'java'
                }
                repositories {
                    mavenCentral()
                }
                dependencies {
                    implementation 'org.apache.maven:maven-plugin-api:3.6.3'
                    implementation 'org.apache.maven.plugin-tools:maven-plugin-annotations:3.6.0'
                }
            """
            project.file("src/main/java/sample/lib/AbstractNameMojo.java") << """
                package sample.lib;
                
                import org.apache.maven.plugin.AbstractMojo;
                import org.apache.maven.plugins.annotations.Parameter;
                
                public abstract class AbstractNameMojo extends AbstractMojo {
                
                  /**
                   * This is the name parameter.
                   */
                  @Parameter(property = "name")
                  private String name;
                
                  public String getName() {
                    return name;
                  }
                }
            """
        }
        TestProject mojoProject = subproject("greeting-mojo") { project ->
            project.withMavenPluginBuildConfiguration()
            project.buildFile << """
                dependencies.implementation project(':base-mojo')
            """
            project.file("src/main/java/sample/plugin/GreetingMojo.java") << """
                package sample.plugin;
                
                import org.apache.maven.plugin.MojoExecutionException;
                import org.apache.maven.plugins.annotations.Mojo;
                import org.apache.maven.plugins.annotations.Parameter;
                import sample.lib.AbstractNameMojo;
                
                /**
                 * Says "Hi" to the user.
                 */
                @Mojo(name = "sayhi")
                public class GreetingMojo extends AbstractNameMojo {
                
                  @Parameter(property = "greeting")
                  private String greeting;
                
                  public String getGreeting() {
                    return greeting;
                  }
                
                  @Override
                  public void execute() throws MojoExecutionException {
                    getLog().info("GreetingMojo: " + getGreeting() + " " + getName());
                  }
                }
            """
        }

        when:
        run(":greeting-mojo:build")

        then:
        mojoProject.pluginDescriptor.getMojo("sayhi").parameters.contains(new DescriptorFile.ParameterDeclaration("name", String, "", false, true, "This is the name parameter."))
    }
}
