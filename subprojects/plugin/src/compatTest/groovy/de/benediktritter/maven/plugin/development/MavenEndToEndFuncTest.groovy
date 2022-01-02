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

class MavenEndToEndFuncTest extends AbstractPluginFuncTest {

    File mavenRepository

    def setup() {
        mavenRepository = dir("maven-repository")
        def buildFileContens = buildFile.text
        buildFile.text = """
            plugins {
                id 'maven-publish'
            }

            $buildFileContens

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

    def "plugin built by Gradle can be used in maven build"() {
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
        mavenBuildResult.contains("touch-maven-plugin:1.0.0:touch")
        mavenBuildResult.contains("BUILD SUCCESS")
        file("$mavenBuild/target/classes/touch.txt").exists()
    }

    def "plugin descriptor build by Gradle equals plugin descriptor build by Maven"() {
        given:
        javaMojo()

        and:
        def mavenBuild = mavenBuildBuildingPlugin()

        when:
        run("build")

        and:
        String mavenBuildResult = mvnCleanPackage(mavenBuild)

        then:
        mavenBuildResult.contains("maven-plugin-plugin")
        mavenBuildResult.contains("BUILD SUCCESS")

        and:
        def mavenPluginDescriptor = DescriptorFile.parse(file("$mavenBuild/target/classes/META-INF/maven/plugin.xml"))
        def mavenHelpDescriptor = DescriptorFile.parse(file("$mavenBuild/target/classes/META-INF/maven/org.example/touch-maven-plugin/plugin-help.xml"))
        mavenPluginDescriptor == pluginDescriptor
        mavenHelpDescriptor == helpDescriptor
    }

    private String mvnCleanPackage(File mavenBuild) {
        def path = "${System.getProperty("java.home")}:${System.getenv("PATH")}"
        "mvn clean package -B -Dmaven.repo.local=$mavenRepository".execute(["PATH=$path"], mavenBuild).text
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
}
