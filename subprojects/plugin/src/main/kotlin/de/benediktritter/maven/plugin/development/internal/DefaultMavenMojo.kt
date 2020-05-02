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

package de.benediktritter.maven.plugin.development.internal

import de.benediktritter.maven.plugin.development.MavenMojo
import de.benediktritter.maven.plugin.development.MavenMojoParameter
import de.benediktritter.maven.plugin.development.MavenMojoParametersSpec
import de.benediktritter.maven.plugin.development.MavenMojo.ExecutionStrategy
import org.apache.maven.plugins.annotations.InstantiationStrategy
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.ResolutionScope
import org.gradle.api.Action
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

open class DefaultMavenMojo(@get:Input val name: String) : MavenMojo, MavenMojoParametersSpec {

    @get:Nested
    val parameters = mutableListOf<DefaultMavenMojoParameter>()

    @get:Input
    @get:Optional
    override var description: String? = null

    @get:Input
    override var implementation: String? = null

    @get:Input
    override var language: String = "java"

    @get:Input
    override var defaultPhase: LifecyclePhase = LifecyclePhase.NONE

    @get:Input
    override var requiresDependencyResolution: ResolutionScope = ResolutionScope.NONE

    @get:Input
    override var requiresDependencyCollection: ResolutionScope = ResolutionScope.NONE

    @get:Input
    override var instantiationStrategy: InstantiationStrategy = InstantiationStrategy.PER_LOOKUP

    @get:Input
    override var executionStrategy: ExecutionStrategy = ExecutionStrategy.ONCE_PER_SESSION

    @get:Input
    override var isRequiresProject: Boolean = true

    @get:Input
    override var isRequiresReports: Boolean = false

    @get:Input
    override var isAggregator: Boolean = false

    @get:Input
    override var isRequiresDirectInvocation: Boolean = false

    @get:Input
    override var isRequiresOnline: Boolean = false

    @get:Input
    override var isInheritByDefault: Boolean = true

    @get:Input
    @get:Optional
    override var configurator: String? = null

    @get:Input
    override var isThreadSafe: Boolean = false

    override fun parameters(configure: Action<in MavenMojoParametersSpec>) = configure.execute(this)

    override fun parameter(name: String, type: String) {
        parameter(name, type) { }
    }

    override fun parameter(name: String, type: String, configure: Action<in MavenMojoParameter>) {
        val parameter = DefaultMavenMojoParameter(name, type)
        configure.execute(parameter)
        parameters.add(parameter)
    }

    override fun parameter(name: String, type: Class<Any>) {
        parameter(name, type.name)
    }

    override fun parameter(name: String, type: Class<Any>, configure: Action<in MavenMojoParameter>) {
        parameter(name, type.name, configure)
    }
}
