plugins {
    id("groovy")
    id("java-gradle-plugin")
    id("java-test-fixtures")
    id("jvm-test-suite")
    id("org.asciidoctor.jvm.convert") version "4.0.4"
    id("org.gradlex.internal.plugin-publish-conventions") version "0.6"
}

group = "org.gradlex"
version = "1.0"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

dependencies {
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

pluginPublishConventions {
    id("${project.group}.${project.name}")
    implementationClass("org.gradlex.maven.plugin.development.MavenPluginDevelopmentPlugin")
    displayName("Maven Plugin Development Gradle Plugin")
    description("Gradle plugin for developing Apache Maven plugins.")
    tags("gradlex", "maven", "mojo", "maven plugin")
    gitHub("https://github.com/gradlex-org/maven-plugin-development")
    website("https://gradlex.org/maven-plugin-development")
    developer {
        id = "britter"
        name = "Benedikt Ritter"
        email = "benedikt@gradlex.org"
    }
}

testing.suites.named<JvmTestSuite>("test") {
    useSpock()
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
        javaLauncher.set(project.javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(17) })
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
                "snippets-path" to "$projectDir/src/docs/snippets"
        ))
    }
}
