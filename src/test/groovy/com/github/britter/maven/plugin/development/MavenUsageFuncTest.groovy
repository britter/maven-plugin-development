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

package com.github.britter.maven.plugin.development

class MavenUsageFuncTest extends AbstractPluginFuncTest {

    File mavenRepository

    File mavenBuild

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
        mavenBuild = dir("mavenBuild")
        file("$mavenBuild/pom.xml") << """
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.example</groupId>
  <artifactId>use-touch-plugin</artifactId>
  <version>1.0-SNAPSHOT</version>
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
    }

    def "plugin built by Gradle can be used in maven build"() {
        given:
        javaMojo()

        when:
        run("publishMavenPublicationToTestRepository")

        then:
        file("$mavenRepository/org/example/touch-maven-plugin/1.0.0/touch-maven-plugin-1.0.0.jar").exists()

        when:
        def mavenBuildResult = "mvn clean package -B -Dmaven.repo.local=$mavenRepository".execute([], mavenBuild).text

        then:
        mavenBuildResult.contains("touch-maven-plugin:1.0.0:touch")
        mavenBuildResult.contains("BUILD SUCCESS")
        file("$mavenBuild/target/classes/touch.txt").exists()
    }
}
