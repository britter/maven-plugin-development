# Maven Plugin Development Gradle Plugin

[![Build Status](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2Fbritter%2Fmaven-plugin-development%2Fbadge%3Fref%3Dmaster&style=flat)](https://actions-badge.atrox.dev/britter/maven-plugin-development/goto?ref=master)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v?label=Plugin%20Portal&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fde%2Fbenediktritter%2Fmaven-plugin-development%2Fde.benediktritter.maven-plugin-development.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/de.benediktritter.maven-plugin-development)

This plugin aims to fill the tiny gap for people who need to create an [Apache Maven](https://maven.apacke.org) plugin from a Gradle build.
To do this the plugin wraps around the [Maven Plugin Tools API](https://maven.apache.org/plugin-tools/) and feeds it with the right inputs from the Gradle build.

## Features

- Automatic generation of a maven plugin descriptor containing all mojos in the selected source set (by default the plugin looks for mojo implementations in the main source set)
- Support for annotation and JavaDoc tag based mojo implementations
- Optional generation of a help mojo implementation

## Usage

Once applied, the plugin adds a [`MavenPluginDevelopmentExtension`](https://github.com/britter/maven-plugin-development/blob/0.1.0/src/main/kotlin/de/benediktritter/maven/plugin/development/MavenPluginDevelopmentExtension.kt) with name `mavenPlugin` to the project.
All meta data for the plugin, e.g. `groupId`, `artifactId`, `description` is extracted from the corresponding project properties.
The only setting that users might want to configure is `generateHelpMojo` which is false be default, meaning not help mojo will be generated.

### Gradle Groovy DSL

```groovy
plugins {
  id 'de.benediktritter.maven-plugin-development' version '0.1.0'
}

mavenPlugin {
  // optional, false by default
  generateHelpMojo = true
}
```

### Gradle Kotlin DSL

```kotlin
plugins {
  id("de.benediktritter.maven-plugin-development") version "0.1.0"
}

mavenPlugin {
  // optional, false by default
  generateHelpMojo.set(true)
}
```

## Extracting mojos from other subprojects

The plugin will create a configuration called `mojo` and add it to the project.
All project dependencies added to this configuration will be searched for mojo implementations.

### Gradle Groovy DSL

```groovy
plugins {
  id 'de.benediktritter.maven-plugin-development' version '0.1.0'
}

dependencies {
  mojo project(":mojo-subproject")
}
```

### Gradle Kotlin DSL

```kotlin
plugins {
  id("de.benediktritter.maven-plugin-development") version "0.1.0"
}

dependencies {
  mojo(project(":mojo-subproject"))
}
```

## Controlling plugin dependencies

By default all dependencies from the runtime classpath will in added to the dependencies blog of the generated plugin descriptor.
The can be changed by configuring the `dependencies` property on the `mavenPlugin` extension.
In the following examples only `org.apache.commons:commons-lang3:3.9` will be added as a dependency to the plugin descriptor: 


### Gradle Groovy DSL

```groovy
plugins {
  id 'de.benediktritter.maven-plugin-development' version '0.1.0'
}

configurations {
  deps
}

dependencies {
  deps "org.apache.commons:commons-lang3:3.9"
  implementation "com.google.guava:guava:28.0-jre"
}

mavenPlugin {
  dependencies = configurations.deps
}
```

### Gradle Kotlin DSL

```kotlin
plugins {
  id("de.benediktritter.maven-plugin-development") version "0.1.0"
}

val deps by configurations.creating

dependencies {
  deps("org.apache.commons:commons-lang3:3.9")
  implementation("com.google.guava:guava:28.0-jre")
}

mavenPlugin {
  dependencies.set(deps)
}
```

## Defining mojos in the build script

If you can't or don't want to use auto detection of mojos there is also a DSL for defining mojos inside the build script:

### Gradle Groovy DSL

```groovy
plugins {
  id 'de.benediktritter.maven-plugin-development' version '0.1.0'
}

mavenPlugin {
  mojos {
    touch {
      implementation = "com.example.MyMojo"
      description = "A super fancy mojo defined in my build.gradle"
      parameters {
        parameter("outputDir", File) {
          defaultValue = "\${project.build.outputDirectory}/myMojoOutput"
          required = false
        }
      }
    }
  }
}
```

### Gradle Kotlin DSL

```kotlin
plugins {
  id("de.benediktritter.maven-plugin-development") version "0.1.0"
}

mavenPlugin {
  mojos {
    create<MavenMojo>("touch") {
      implementation = "com.example.MyMojo"
      description = "A super fancy mojo defined in my build.gradle"
      parameters {
        parameter("outputDir", File::class.java) {
          defaultValue = "\${project.build.outputDirectory}/myMojoOutput"
          required = false
        }
      }
    }
  }
}
```

## Contribution policy

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License

This code is open source software licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html).
