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
    `kotlin-dsl`
    groovy
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
}

group = "com.github.britter"
description = "Gradle plugin for build Maven plugins by generating the necessary metadata files"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(platform("org.apache.maven.plugin-tools:maven-plugin-tools:3.6.0"))
    implementation("org.apache.maven.plugin-tools:maven-plugin-tools-api")
    implementation("org.apache.maven.plugin-tools:maven-plugin-tools-annotations")
    implementation("org.apache.maven.plugin-tools:maven-plugin-tools-generators")
    implementation("org.apache.maven:maven-plugin-api:3.0")
    implementation("org.sonatype.sisu:sisu-inject-plexus:1.4.2") {
        because("it is needed to implement the plexus logging adapter")
    }

    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
}

gradlePlugin {
    plugins.create("mavenPluginMetadata") {
        id = "com.github.britter.maven-plugin-metadata"
        displayName = "Maven plugin metadata plugin"
        description = project.description
        implementationClass = "com.github.britter.mavenpluginmetadata.MavenPluginMetadataPlugin"
    }
}
