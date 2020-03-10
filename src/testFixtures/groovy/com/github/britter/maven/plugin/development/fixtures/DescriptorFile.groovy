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

package com.github.britter.maven.plugin.development.fixtures

class DescriptorFile {

    private final def xml
    private final String rawContent

    DescriptorFile(File file) {
        assert file.exists(): "Descriptor $file does not exist"
        this.rawContent = file.text
        this.xml = new XmlSlurper().parse(file)
    }

    boolean hasName(String expectedName) {
        xml.name == expectedName
    }

    boolean hasDescription(String expectedDescription) {
        xml.description == expectedDescription
    }

    boolean hasGroupId(String expectedGroupId) {
        xml.groupId == expectedGroupId
    }

    boolean hasArtifactId(String expectedArtifactId) {
        xml.artifactId == expectedArtifactId
    }

    boolean hasVersion(String expectedVersion) {
        xml.version == expectedVersion
    }

    boolean hasGoalPrefix(String expectedGoalPrefix) {
        xml.goalPrefix == expectedGoalPrefix
    }

    boolean hasGoal(String expectedGoal) {
        xml.mojos.mojo.find { mojo ->
            mojo.goal == expectedGoal
        }.size() == 1
    }

    boolean hasDependency(String dependencyNotation) {
        def coords = dependencyNotation.split(":")
        xml.dependencies.dependency.find { dependency ->
            dependency.groupId == coords[0] &&
            dependency.artifactId == coords[1] &&
            dependency.version == coords[2] &&
            dependency.type == "jar"
        }.size() == 1
    }

    boolean hasNoDependencies() {
        return xml.dependencies.isEmpty
    }

    String getText() {
        xml
    }
}
