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

    private final String content

    DescriptorFile(File file) {
        assert file.exists(): "Descriptor $file does not exist"
        this.content = file.text
    }

    boolean hasName(String expectedName) {
        content.contains("<name>$expectedName</name>")
    }

    boolean hasDescription(String expectedDescription) {
        content.contains("<description>$expectedDescription</description>")
    }

    boolean hasGroupId(String expectedGroupId) {
        content.contains("<groupId>$expectedGroupId</groupId>")
    }

    boolean hasArtifactId(String expectedArtifactId) {
        content.contains("<artifactId>$expectedArtifactId</artifactId>")
    }

    boolean hasVersion(String expectedVersion) {
        content.contains("<version>$expectedVersion</version>")
    }

    boolean hasGoalPrefix(String expectedGoalPrefix) {
        content.contains("<goalPrefix>$expectedGoalPrefix</goalPrefix>")
    }

    boolean hasGoal(String expectedGoal) {
        content.contains("<goal>$expectedGoal</goal>")
    }

    boolean hasDependency(String dependencyNotation) {
        def coords = dependencyNotation.split(":")
        return (content.contains("<groupId>${coords[0]}</groupId>")
                && content.contains("<artifactId>${coords[1]}</artifactId>")
                && content.contains("<version>${coords[2]}</version>")
                && content.contains("<type>jar</type>"))
    }

    boolean hasNoDependencies() {
        return !content.contains("<dependency>")
    }
}
