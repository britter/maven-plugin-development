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

import groovy.transform.Immutable

@Immutable
class DescriptorFile {

    String name
    String description
    String groupId
    String artifactId
    String version
    String goalPrefix
    Set<MojoDeclaration> mojos
    Set<DependencyDeclaration> dependencies

    static DescriptorFile parse(File file) {
        assert file.exists(): "Descriptor $file does not exist"
        def xml = new XmlSlurper().parse(file)
        def mojos = [] as Set
        xml.mojos.mojo.each {
            def mojo = new MojoDeclaration(
                    it.goal.text(),
                    it.description.text(),
                    it.requiresDirectInvocation.text().toBoolean(),
                    it.requiresProject.text().toBoolean(),
                    it.requiresReports.text().toBoolean(),
                    it.aggregator.text().toBoolean(),
                    it.requiresOnline.text().toBoolean(),
                    it.inheritedByDefault.text().toBoolean(),
                    it.phase.text(),
                    it.implementation.text(),
                    it.language.text(),
                    it.instantiationStrategy.text(),
                    it.executionStrategy.text(),
                    it.threadSafe.text().toBoolean()
            )
            assert mojos << mojo: "Duplicate mojo declaration $mojo"
        }
        def dependencies = [] as Set
        xml.dependencies.dependency.each {
            def dependency = new DependencyDeclaration(it.groupId.text(), it.artifactId.text(), it.version.text(), it.type.text())
            assert dependencies << dependency: "Duplicate dependency declaration $dependency"
        }
        return new DescriptorFile(
                xml.name.text(),
                xml.description.text(),
                xml.groupId.text(),
                xml.artifactId.text(),
                xml.version.text(),
                xml.goalPrefix.text(),
                mojos,
                dependencies
        )
    }

    boolean hasGoal(String expectedGoal) {
        mojos.any { it.goal == expectedGoal }
    }

    boolean hasDependency(String dependencyNotation) {
        def coords = dependencyNotation.split(":")
        dependencies.any {
            it.groupId == coords[0] &&
            it.artifactId == coords[1] &&
            it.version == coords[2] &&
            it.type == "jar"
        }
    }

    boolean hasNoDependencies() {
        return dependencies.isEmpty()
    }

    @Immutable
    static class DependencyDeclaration {
        String groupId
        String artifactId
        String version
        String type
    }

    @Immutable
    static class MojoDeclaration {
        String goal
        String description
        boolean requiresDirectInvocation
        boolean requiresProject
        boolean requiresReports
        boolean aggregator
        boolean requiresOnline
        boolean inheritedByDefault
        String phase
        String implementation
        String language
        String instantiationStrategy
        String executionStrategy
        boolean threadSafe
    }
}
