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

plugins {
    id("groovy")
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("org.ajoberstar.git-publish") version "3.0.0"
}

repositories {
    exclusiveContent {
        forRepository {
            maven(url = uri("https://repo.gradle.org/gradle/libs"))
        }
        filter {
            includeModule("org.gradle", "sample-check")
            includeModule("org.gradle", "sample-discovery")
        }
    }
    mavenCentral()
}

dependencies {
    testImplementation(libs.groovy)
    testImplementation(gradleTestKit())
    testImplementation(libs.exemplar.sampleCheck)
    testRuntimeOnly(project(":plugin", "pluginUnderTestMetadata"))
}

tasks {
    test {
        inputs.dir("src/docs/snippets")
    }

    asciidoctor {
        outputOptions {
            separateOutputDirs = false
        }

        attributes(mapOf(
                "docinfodir" to "src/docs/asciidoc",
                "docinfo" to "shared",
                "source-highlighter" to "prettify",
                "tabsize" to "4",
                "toc" to "left",
                "icons" to "font",
                "sectanchors" to true,
                "idprefix" to "",
                "idseparator" to "-",
                "gh-issue" to "https://github.com/britter/maven-plugin-development/issues/",
                "snippets-path" to "$projectDir/src/docs/snippets"
        ))
    }
}

val asciidoctor by tasks.existing

gitPublish {
    repoUri.set("https://github.com/britter/maven-plugin-development")
    branch.set("gh-pages")
    sign.set(false)

    contents {
        from(asciidoctor)
    }
}
