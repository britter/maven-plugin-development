plugins {
    id("org.gradlex.maven-plugin-development") version "1.0.3"
}

// tag::help-mojo[]
mavenPlugin {
    helpMojoPackage.set("org.example.help")
}
// end::help-mojo[]
