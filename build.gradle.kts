plugins {
    id("groovy")
    id("java-gradle-plugin")
    id("java-test-fixtures")
    id("jvm-test-suite")
    `kotlin-dsl`
    alias(libs.plugins.kotlin)
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.asciidoctor)
    alias(libs.plugins.gitPublish)
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

testing.suites.register<JvmTestSuite>("testSamples") {
    useJUnit()
    dependencies {
        implementation(gradleTestKit())
        implementation(libs.exemplar.sampleCheck)
    }
    targets.all {
        testTask.configure {
            inputs.dir("src/docs/snippets")
                .withPathSensitivity(PathSensitivity.RELATIVE)
                .withPropertyName("snippets")
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
    jar {
        from(rootProject.file("LICENSE.txt")) {
            into("META-INF")
        }
    }
    asciidoctor {
        notCompatibleWithConfigurationCache("See https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/564")
        inputs.dir("src/docs/snippets")
            .withPathSensitivity(PathSensitivity.RELATIVE)
            .withPropertyName("snippets")
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
                "gh-pr" to "https://github.com/britter/maven-plugin-development/pull/",
                "snippets-path" to "$projectDir/src/docs/snippets"
        ))
    }
    register("release") {
        dependsOn("publishPlugins", ":gitPublishPush")
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

val asciidoctor by tasks.existing

gitPublish {
    repoUri.set("https://github.com/britter/maven-plugin-development")
    branch.set("gh-pages")
    sign.set(false)

    contents {
        from(asciidoctor)
    }
}

