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
    `java-gradle-plugin`
    `java-test-fixtures`
    `kotlin-dsl`
    `maven-publish`
    groovy
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    id("com.gradle.plugin-publish") version "0.12.0"
}

group = "de.benediktritter"
description = "Gradle plugin for developing Apache Maven plugins"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    api(platform("org.apache.maven.plugin-tools:maven-plugin-tools:3.6.0")) {
        because("the version for other dependencies in api would be missing otherwise")
    }
    api("org.apache.maven.plugin-tools:maven-plugin-annotations") {
        because("MavenMojo references types from this artifact")
    }
    implementation("org.apache.maven.plugin-tools:maven-plugin-tools-api")
    implementation("org.apache.maven.plugin-tools:maven-plugin-tools-annotations")
    implementation("org.apache.maven.plugin-tools:maven-plugin-tools-java")
    implementation("org.apache.maven.plugin-tools:maven-plugin-tools-generators")
    implementation("org.apache.maven:maven-plugin-api:3.0")
    implementation("org.sonatype.sisu:sisu-inject-plexus:1.4.2") {
        because("it is needed to implement the plexus logging adapter")
    }
    implementation("org.codehaus.plexus:plexus-velocity:1.1.8") {
        because("it is needed to generate the help mojo")
    }
    constraints {
        implementation("com.thoughtworks.qdox:qdox") {
            version {
                require("2.0-M9")
                prefer("2.0.0")
            }
            because("we need the fix for https://github.com/paul-hammant/qdox/issues/43")
        }
    }

    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
    testFixturesImplementation("junit:junit:4.12")
    testFixturesImplementation("org.apache.commons:commons-lang3:3.10")
}

tasks.jar {
    from(rootProject.file("LICENSE.txt")) {
        into("META-INF")
    }
}

gradlePlugin {
    plugins.create("mavenPluginDevelopment") {
        id = "de.benediktritter.maven-plugin-development"
        displayName = "Maven plugin development plugin"
        description = project.description
        implementationClass = "de.benediktritter.maven.plugin.development.MavenPluginDevelopmentPlugin"
    }
}

pluginBundle {
    description = project.description
    website = "https://britter.github.io/maven-plugin-development"
    vcsUrl = "https://github.com/britter/maven-plugin-development"
    tags = listOf("maven", "mojo", "maven plugin")
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

    val publication = configurations.create("pluginUnderTestMetadata") {
        isCanBeConsumed = true
        isCanBeResolved = false
        outgoing.artifact(pluginUnderTestMetadata.flatMap { it.outputDirectory })
    }
}
