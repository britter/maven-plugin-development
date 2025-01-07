plugins {
    id("de.benediktritter.maven-plugin-development") version "0.4.3"
}

// tag::dependencies[]
val deps by configurations.creating

dependencies {
    deps("org.apache.commons:commons-lang3:3.9")
    implementation("com.google.guava:guava:28.0-jre")
}

mavenPlugin {
    dependencies.set(deps)
}
// end::dependencies[]
