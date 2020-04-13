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

package de.benediktritter.maven.plugin.development.model

import org.apache.maven.plugins.annotations.InstantiationStrategy
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.ResolutionScope
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.domainObjectContainer

/**
 * Representation of a mojo.
 *
 * See [@Mojo][org.apache.maven.plugins.annotations.Mojo] for documentation and
 * default values of the individual fields.
 *
 * @since 0.2.0
 */
open class MojoDeclaration(@get:Input val name: String) : ParametersSpec {

    val parameters = mutableListOf<ParameterDeclaration>()

    @get:Input
    @get:Optional
    var description: String? = null

    @get:Input
    var implementation: String? = null

    @get:Input
    var language: String = "java"

    @get:Input
    var defaultPhase: LifecyclePhase = LifecyclePhase.NONE

    @get:Input
    var requiresDependencyResolution: ResolutionScope = ResolutionScope.NONE

    @get:Input
    var requiresDependencyCollection: ResolutionScope = ResolutionScope.NONE

    @get:Input
    var instantiationStrategy: InstantiationStrategy = InstantiationStrategy.PER_LOOKUP

    @get:Input
    var executionStrategy: ExecutionStrategy = ExecutionStrategy.ONCE_PER_SESSION

    @get:Input
    var isRequiresProject: Boolean = true

    @get:Input
    var isRequiresReports: Boolean = false

    @get:Input
    var isAggregator: Boolean = false

    @get:Input
    var isRequiresDirectInvocation: Boolean = false

    @get:Input
    var isRequiresOnline: Boolean = false

    @get:Input
    var isInheritByDefault: Boolean = true

    @get:Input
    @get:Optional
    var configurator: String? = null

    @get:Input
    var isThreadSafe: Boolean = false

    fun parameters(configure: Action<in ParametersSpec>) = configure.execute(this)

    override fun parameter(name: String, type: String) {
        parameter(name, type) { }
    }

    override fun parameter(name: String, type: String, configure: Action<in ParameterDeclaration>) {
        val parameter = ParameterDeclaration(name, type)
        configure.execute(parameter)
        parameters.add(parameter)
    }

    override fun parameter(name: String, type: Class<Any>) {
        parameter(name, type.name)
    }

    override fun parameter(name: String, type: Class<Any>, configure: Action<in ParameterDeclaration>) {
        parameter(name, type.name, configure)
    }
}
