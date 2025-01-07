# Maven Plugin Development Gradle plugin - Changelog

## Version 1.0

This release marks the move of this plugin to the [GradleX project](https://github.com/gradlex-org).
Consequently, this is a breaking release with features being dropped, plugin ID and code coordinate changes, and the implementation language changing from Kotlin to Java.
The minimal required Gradle version has been increased to 7.0.
Lower versions of Gradle might be supported but are not tested.

* [Fixed] [#57](https://github.com/britter/maven-plugin-development/issues/57) Remove javaClassesDir from GenerateMavenPluginDescriptorTask.
* [Fixed] [#210](https://github.com/britter/maven-plugin-development/issues/210) Drop feature: Mojo source set. 
    It's no longer possible to define a dedicated source set for defining the mojo.
    Use a dedicated project instead.
* [Fixed] [#212](https://github.com/britter/maven-plugin-development/issues/212) Drop feature: Finding mojos in project dependencies.
    The plugin no longer searches for mojo implementations in dependent projects since this is not the way of modelling aggregation of build results in Gradle.
    Users who rely on this use case should instead apply this plugin to all projects with mojo implementations, set up a variant that exposes the descriptor, and then merge all exposed descriptor variants in an aggregator project.
* [Fixed] [209](https://github.com/britter/maven-plugin-development/issues/209) Drop feature: Mojo DSL.
    This feature was dropped without replacement.
    Annotate mojos using annotations defined in `org.apache.maven.plugin-tools:maven-plugin-annotations` instead of declaring them in your build script.
* [Fixed] [278](https://github.com/britter/maven-plugin-development/issues/278) Remove deprecated code.
    In particular this the `generateHelpMojo` property was removed.
    Users should configure `helpMojoPackage` with the desired target package for the generated help mojo.
* [Fixed] [8](https://github.com/britter/maven-plugin-development/issues/278) Support configuration cache.
* [Fixed] [38](https://github.com/britter/maven-plugin-development/issues/278) Don't use Property for lazy types.
    Instead of declaring fields of type `Property<FileCollection>` the `GenerateMavenPluginDescriptorTask` not declares fields of type `ConfigurableFileCollection`, which is more idiomatic.

## Version 0.4.3

* [Fixed] [#166](https://github.com/britter/maven-plugin-development/issues/166) Fix deprecation warnings. Thanks to [Zongle Wang](https://github.com/Goooler).

## Version 0.4.2

* [Fixed] [#94](https://github.com/britter/maven-plugin-development/pull/94) Make plugin compatible with Gradle 8.x

## Version 0.4.1

* [Fixed] [#89](https://github.com/britter/maven-plugin-development/pull/89) Make plugin compatible with Java 17

## Version 0.4.0

* [Fixed] [#13](https://github.com/britter/maven-plugin-development/issues/13) Descriptor generation is up to date although mojo dependency changed
* [Fixed] [#73](https://github.com/britter/maven-plugin-development/issues/73) `@Parameter` in abstract classes in other modules are ignored

### Breaking changes

* The `mojo` configuration was removed without replacement.
  The plugin now searches the compile classpath instead.
* Property `mojoDependencies` dropped from `GenerateMavenPluginDescriptorTask` without replacement.

## Version 0.3.1

* [New] [#7](https://github.com/britter/maven-plugin-development/issues/7) Build cache support
* [New] [#26](https://github.com/britter/maven-plugin-development/issues/26) Plugin jar contains LICENSE file
* [New] [#47](https://github.com/britter/maven-plugin-development/issues/47) Additional project metadata added to published plugin POMs
* [New] [#35](https://github.com/britter/maven-plugin-development/issues/35) More idiomatic task dependency modelling

## Version 0.3.0

*Note:* This release includes a change in behavior of the HelpMojo generation.
Before 0.3.0 generation of a HelpMojo could be turned on by setting `generateHelpMojo` to `true`.
Starting with 0.3.0 `generateHelpMojo` is deprecated and replaced by `helpMojoPackage`.
Users now need to define the package of the HelpMojo.
This fixes problems with up-to-date checking (see [#16](https://github.com/britter/maven-plugin-development/issues/16)).

* [Fixed] [#34](https://github.com/britter/maven-plugin-development/issues/34) Task descriptions for help and descriptor task are swapped
* [Fixed] [#16](https://github.com/britter/maven-plugin-development/issues/16) Help mojo task is never up to date because descriptor tasks modifies outputs

### Deprecations

* The `generateHelpMojo` setting has been deprecated in favor of `helpMojoPackage`

### Breaking changes

* Class `de.benediktritter.maven.plugin.development.task.HelpGeneratorAccessor` has been removed

## Version 0.2.1

* [New] [#22](https://github.com/britter/maven-plugin-development/issues/22) Tasks provide proper descriptions
* [Fixed] [#19](https://github.com/britter/maven-plugin-development/issues/19) Build fails when `java` plugin is not applied explicitly
* [Fixed] [#23](https://github.com/britter/maven-plugin-development/issues/23) Typing of `parameters` API is incorrect

## Version 0.2.0

## New Features

* [New] [#4](https://github.com/britter/maven-plugin-development/issues/4) Add DSL for declaring mojos in the build script
* [New] [#12](https://github.com/britter/maven-plugin-development/issues/12) Provide way to configure plugin dependencies
* [New] [#11](https://github.com/britter/maven-plugin-development/issues/11) Define dedicated configuration for projects providing mojos
* [New] [#3](https://github.com/britter/maven-plugin-development/issues/3) Add support for mojos from other projects
* [Fixed] [#9](https://github.com/britter/maven-plugin-development/issues/9) Force version upgrade of qdox

### Breaking changes

* Package `de.benediktritter.maven.plugin.development.model` has been removed.

## Version 0.1.0

Initial release
