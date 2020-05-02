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
import org.gradle.kotlin.dsl.*

plugins {
    // TODO remove after 0.2.1 release
    java
    id("de.benediktritter.maven-plugin-development") version "0.2.0"
}

// tag::buildscript-dsl[]
mavenPlugin {
    mojos {
        create("touch") {
            implementation = "com.example.MyMojo"
            description = "A super fancy mojo defined in my build.gradle"
            parameters {
                parameter("outputDir", "java.io.File") {
                    defaultValue = "\${project.build.outputDirectory}/myMojoOutput"
                    isRequired = false
                }
            }
        }
    }
}
// end::buildscript-dsl[]