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

import io.takari.maven.testing.executor.MavenExecutionResult
import io.takari.maven.testing.executor.MavenRuntime
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder
import io.takari.maven.testing.executor.MavenVersions
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner
import org.gradle.testkit.runner.GradleRunner
import org.gradlex.maven.plugin.development.fixtures.DescriptorFile
import org.gradlex.maven.plugin.development.fixtures.TestRootProject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import java.lang.management.ManagementFactory

/**
 * This is implemented as a JUnit 4 test, so we can use https://github.com/takari/takari-plugin-testing-project
 */
@RunWith(MavenJUnitTestRunner)
@MavenVersions("3.9.9")
class MavenEndToEndFuncTest {

    @Delegate
    @Rule
    public TestRootProject project = new TestRootProject()

    File mavenRepository

    MavenRuntime maven;

    MavenEndToEndFuncTest(MavenRuntimeBuilder mavenBuilder) {
        this.maven = mavenBuilder.withCliOptions("-B", "-U")
                // not using the forked builder makes the tests fail with
                //   libXext.so.6: cannot open shared object file: No such file or director
                // which may be a NixOS specific problem.
                // Using the forked builder works.
                .forkedBuilder()
                .build()
    }

    @Before
    void setup() {
        settingsFile << "rootProject.name=\"touch-maven-plugin\""
        withMavenPluginBuildConfiguration()
        mavenRepository = dir("maven-repository")
        def buildFileContents = buildFile.text
        buildFile.text = """
            plugins {
                id 'maven-publish'
            }

            $buildFileContents

            publishing {
                publications {
                    maven(MavenPublication) {
                        from components.java
                    }
                }
                repositories {
                    maven {
                        name = "Test"
                        url = "$mavenRepository"
                    }
                }
            }
        """
    }

    @Test
    void "plugin built by Gradle can be used in maven build"() {
        given:
        javaMojo()

        and:
        def mavenBuild = mavenBuildUsingPlugin()

        when:
        run("publishMavenPublicationToTestRepository")

        then:
        file("$mavenRepository/org/example/touch-maven-plugin/1.0.0/touch-maven-plugin-1.0.0.jar").exists()

        when:
        def mavenBuildResult = mvnCleanPackage(mavenBuild)

        then:
        mavenBuildResult.assertLogText("[INFO] --- touch:1.0.0:touch (default) @ use-touch-plugin ---")
        file("$mavenBuild/target/classes/touch.txt").exists()
    }

    @Test
    void "plugin descriptor build by Gradle equals plugin descriptor build by Maven"() {
        given:
        javaMojo()

        and:
        def mavenBuild = mavenBuildBuildingPlugin()

        when:
        run("build")

        and:
        MavenExecutionResult mavenBuildResult = mvnCleanPackage(mavenBuild)

        then:
        mavenBuildResult.assertLogText("maven-plugin-plugin")

        and:
        def mavenPluginDescriptor = DescriptorFile.parse(file("$mavenBuild/target/classes/META-INF/maven/plugin.xml"))
        def mavenHelpDescriptor = DescriptorFile.parse(file("$mavenBuild/target/classes/META-INF/maven/org.example/touch-maven-plugin/plugin-help.xml"))
        mavenPluginDescriptor == pluginDescriptor
        mavenHelpDescriptor == helpDescriptor
    }

    private MavenExecutionResult mvnCleanPackage(File mavenBuild) {
        maven.forProject(mavenBuild)
            .withCliOption("-Dmaven.repo.local=$mavenRepository")
            .execute("clean", "package")
            .assertErrorFreeLog()
    }

    File mavenBuildUsingPlugin() {
        def mavenBuild = dir("mavenBuild")
        file("$mavenBuild/pom.xml") << """
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.example</groupId>
  <artifactId>use-touch-plugin</artifactId>
  <version>1.0.0</version>
  <description>A maven plugin with a mojo that can touch it!</description>
  <properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
  </properties>
  <build>
    <plugins>
      <plugin>
        <groupId>org.example</groupId>
        <artifactId>touch-maven-plugin</artifactId>
        <version>1.0.0</version>
        <executions>
          <execution>
            <goals>
              <goal>touch</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
        """
        mavenBuild
    }

    File mavenBuildBuildingPlugin() {
        file("pom.xml") << """
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.example</groupId>
  <artifactId>touch-maven-plugin</artifactId>
  <packaging>maven-plugin</packaging>
  <version>1.0.0</version>
  <description>A maven plugin with a mojo that can touch it!</description>
  <properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.apache.maven</groupId>
      <artifactId>maven-plugin-api</artifactId>
      <version>3.6.3</version>
    </dependency>
    <dependency>
      <groupId>org.apache.maven.plugin-tools</groupId>
      <artifactId>maven-plugin-annotations</artifactId>
      <version>3.6.0</version>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-plugin-plugin</artifactId>
        <version>3.6.0</version>
      </plugin>
    </plugins>
  </build>
</project>
        """
        project.projectDir
    }

    def run(String... args) {
        runner(args).build()
    }

    def runAndFail(String... args) {
        runner(args).buildAndFail()
    }

    def runner(String... args) {
        GradleRunner.create()
                .forwardOutput()
                .withDebug(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0)
                .withPluginClasspath()
                .withArguments([*args, "-s"])
                .withProjectDir(project.projectDir)
    }
}
