# Maven Plugin Development Gradle Plugin

[![Build Status](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2Fgradlex-org%2Fmaven-plugin-development%2Fbadge%3Fref%3Dmain&style=flat)](https://actions-badge.atrox.dev/gradlex-org/maven-plugin-development/goto?ref=main)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v?label=Plugin%20Portal&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Forg%2Fgradlex%2Fmaven-plugin-development%2Forg.gradlex.maven-plugin-development.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/org.gradlex.maven-plugin-development)

This plugin aims to fill the tiny gap for people who need to create an [Apache Maven](https://maven.apache.org) plugin from a Gradle build.
To do this the plugin wraps around the [Maven Plugin Tools API](https://maven.apache.org/plugin-tools/) and feeds it with the right inputs from the Gradle build.

Compatible with Gradle 7.5 or later.

## Features

- Automatic generation of a maven plugin descriptor containing all mojos in the selected source set (by default the plugin looks for mojo implementations in the main source set)
- Support for annotation and JavaDoc tag based mojo implementations
- Optional generation of a help mojo implementation

## Usage

Please see the [plugin documentation](https://gradlex.org/maven-plugin-development).
For a self-contained example please the [example folder](https://github.com/gradlex-org/maven-plugin-development/tree/main/sample).

## Contribution policy

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License

This code is open source software licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.html).
