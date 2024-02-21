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
    id("idea")
    id("java-gradle-plugin")
    id("java-test-fixtures")
    id("maven-publish")
    `kotlin-dsl`
    alias(libs.plugins.kotlin)
    alias(libs.plugins.pluginPublish)
}

group = "de.benediktritter"
description = "Gradle plugin for developing Apache Maven plugins"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.stdlibJdk8)

    api(platform(libs.mavenPluginTools.bom)) {
        because("the version for other dependencies in api would be missing otherwise")
    }
    implementation(libs.mavenPluginTools.api)
    implementation(libs.mavenPluginTools.annotations)
    implementation(libs.mavenPluginTools.java)
    implementation(libs.mavenPluginTools.generators)

    api(libs.mavenPlugin.annotations) {
        because("MavenMojo references types from this artifact")
    }
    implementation(libs.mavenPlugin.api)

    implementation(libs.sisu.injectPlexus) {
        because("it is needed to implement the plexus logging adapter")
    }
    implementation(libs.plexus.velocity) {
        because("it is needed to generate the help mojo")
    }
    constraints {
        implementation(libs.qdox) {
            because("we need the fix for https://github.com/paul-hammant/qdox/issues/43")
        }
    }

    testImplementation(libs.bundles.spock)
    testImplementation(testFixtures(project))
    testFixturesImplementation(libs.junit4)
    testFixturesImplementation(libs.commonsLang)
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
    jar {
        from(rootProject.file("LICENSE.txt")) {
            into("META-INF")
        }
    }
}

gradlePlugin {
    website.set("https://britter.github.io/maven-plugin-development")
    vcsUrl.set("https://github.com/britter/maven-plugin-development")
    plugins.create("mavenPluginDevelopment") {
        id = "de.benediktritter.maven-plugin-development"
        displayName = "Maven plugin development plugin"
        description = project.description
        implementationClass = "de.benediktritter.maven.plugin.development.MavenPluginDevelopmentPlugin"
        tags.set(listOf("maven", "mojo", "maven plugin"))
    }
}

publishing {
    publications.withType<MavenPublication>() {
        versionMapping {
            usage("java-api") {
                fromResolutionResult()
            }
            usage("java-runtime") {
                fromResolutionResult()
            }
        }
        pom {
            description.set(project.description)
            url.set("https://github.com/britter/maven-development-plugin")
            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/britter/maven-development-plugin.git")
                developerConnection.set("scm:git:ssh://github.com/britter/maven-development-plugin.git")
                url.set("https://github.com/britter/maven-development-plugin")
            }
        }
    }
}

// workaround for https://github.com/gradle/gradle/issues/13980
if (!hasProperty("release")) {
    val pluginUnderTestMetadata by tasks.existing(PluginUnderTestMetadata::class)

    configurations.create("pluginUnderTestMetadata") {
        isCanBeConsumed = true
        isCanBeResolved = false
        outgoing.artifact(pluginUnderTestMetadata.flatMap { it.outputDirectory })
    }
}
