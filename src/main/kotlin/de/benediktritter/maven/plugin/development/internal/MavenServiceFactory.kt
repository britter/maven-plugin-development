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

import org.apache.maven.tools.plugin.extractor.MojoDescriptorExtractor
import org.apache.maven.tools.plugin.extractor.annotations.JavaAnnotationsMojoDescriptorExtractor
import org.apache.maven.tools.plugin.extractor.annotations.converter.JavadocBlockTagsToXhtmlConverter
import org.apache.maven.tools.plugin.extractor.annotations.converter.JavadocInlineTagsToXhtmlConverter
import org.apache.maven.tools.plugin.extractor.annotations.scanner.DefaultMojoAnnotationsScanner
import org.apache.maven.tools.plugin.extractor.annotations.scanner.MojoAnnotationsScanner
import org.apache.maven.tools.plugin.extractor.javadoc.JavaJavadocMojoDescriptorExtractor
import org.apache.maven.tools.plugin.scanner.DefaultMojoScanner
import org.apache.maven.tools.plugin.scanner.MojoScanner

object MavenServiceFactory {

    fun createMojoScanner(loggerAdapter: MavenLoggerAdapter): MojoScanner {
        return DefaultMojoScanner(
                mapOf("java-annotations" to annotationExtractor(loggerAdapter),
                        "java-javadoc" to javadocExtractor(loggerAdapter)))
                .also {
                    it.enableLogging(loggerAdapter)
                }
    }

    private fun annotationExtractor(loggerAdapter: MavenLoggerAdapter): MojoDescriptorExtractor {
        val extractor = JavaAnnotationsMojoDescriptorExtractor()

        val annotationsScanner: MojoAnnotationsScanner = DefaultMojoAnnotationsScanner().also {
            it.enableLogging(loggerAdapter)
        }
        setField(extractor, "mojoAnnotationsScanner", annotationsScanner)

        // TODO this needs a map of tag converts, see the org.apache.maven.tools.plugin.extractor.annotations.converter.tag
        //  in org.apache.maven.plugin-tools:maven-plugin-tools-annotations
        val javadocInlineTagsToHtmlConverter = JavadocInlineTagsToXhtmlConverter(mapOf())
        setField(extractor, "javadocInlineTagsToHtmlConverter", javadocInlineTagsToHtmlConverter)

        val javadocBlockTagsToHtmlConverter = JavadocBlockTagsToXhtmlConverter(javadocInlineTagsToHtmlConverter, mapOf())
        setField(extractor, "javadocBlockTagsToHtmlConverter", javadocBlockTagsToHtmlConverter)

        return extractor
    }

    private fun javadocExtractor(loggerAdapter: MavenLoggerAdapter): MojoDescriptorExtractor {
        return JavaJavadocMojoDescriptorExtractor().also { it.enableLogging(loggerAdapter) }
    }

    private fun setField(instance: Any, fieldName: String, value: Any) {
        val instanceClass = instance.javaClass
        val field = instanceClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(instance, value)
    }
}
