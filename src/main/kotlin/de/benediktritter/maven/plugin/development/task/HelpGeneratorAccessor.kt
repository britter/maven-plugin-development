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

package de.benediktritter.maven.plugin.development.task

import org.apache.maven.plugin.logging.Log
import org.apache.maven.tools.plugin.PluginToolsRequest
import org.apache.maven.tools.plugin.generator.PluginHelpGenerator
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible

/**
 * Provides access to the package private PluginHelpGenerator#rewriteHelpMojo method
 */
object HelpGeneratorAccessor {

    fun rewriteHelpMojo(request: PluginToolsRequest, log: Log) {
        val rewriteFunction = PluginHelpGenerator::class.functions.find { it.name == "rewriteHelpMojo" }!!
        rewriteFunction.isAccessible = true
        rewriteFunction.call(request, log)
    }
}
