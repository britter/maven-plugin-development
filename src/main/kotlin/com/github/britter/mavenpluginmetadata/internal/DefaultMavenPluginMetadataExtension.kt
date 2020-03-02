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

package com.github.britter.mavenpluginmetadata.internal

import com.github.britter.mavenpluginmetadata.MavenPluginDescriptor
import com.github.britter.mavenpluginmetadata.MavenPluginMetadataExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.the
import javax.inject.Inject

open class DefaultMavenPluginMetadataExtension @Inject constructor(project: Project) : MavenPluginMetadataExtension {

    val pluginDescriptor = DefaultMavenPluginDescriptor(project)

    override val sourceSet: Property<SourceSet> = project.objects.property<SourceSet>()
            .convention(project.provider { project.the<SourceSetContainer>()["main"] })

    override fun pluginDescriptor(action: Action<in MavenPluginDescriptor>) {
        action.execute(pluginDescriptor)
    }

}

