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
    id("com.gradle.enterprise") version("3.6.3")
    id("com.gradle.common-custom-user-data-gradle-plugin") version "1.4.2"
}

rootProject.name = "maven-plugin-development"

include(":documentation")
include(":plugin")
includeBuild("sample/gradle-producer-build")

rootProject.children.forEach {
    it.projectDir = file("subprojects/${it.name}")
    it.buildFileName = "${it.name}.gradle.kts"
}

if (System.getenv("CI") == "true") {
    gradleEnterprise {
        buildScan {
            publishAlways()
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}

enableFeaturePreview("VERSION_CATALOGS")
