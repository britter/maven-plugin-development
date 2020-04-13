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

import org.apache.maven.plugins.annotations.InstantiationStrategy
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.ResolutionScope
import org.gradle.api.Action
/**
 * Representation of a mojo.
 *
 * See [@Mojo][org.apache.maven.plugins.annotations.Mojo] for documentation and
 * default values of the individual fields.
 *
 * @since 0.2.0
 */
interface MavenMojo {

    /**
     * Valid values for [@Mojo.executionStrategy()][org.apache.maven.plugins.annotations.Mojo.executionStrategy]
     */
    enum class ExecutionStrategy(private val id: String) {
        ONCE_PER_SESSION("once-per-session"),
        ALWAYS("always");

        fun id(): String = id
    }

    var description: String?

    var implementation: String?

    var language: String

    var defaultPhase: LifecyclePhase

    var requiresDependencyResolution: ResolutionScope

    var requiresDependencyCollection: ResolutionScope

    var instantiationStrategy: InstantiationStrategy

    var executionStrategy: ExecutionStrategy

    var isRequiresProject: Boolean

    var isRequiresReports: Boolean

    var isAggregator: Boolean

    var isRequiresDirectInvocation: Boolean

    var isRequiresOnline: Boolean

    var isInheritByDefault: Boolean

    var configurator: String?

    var isThreadSafe: Boolean

    fun parameters(configure: Action<in MavenMojoParametersSpec>)
}
