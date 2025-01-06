package de.benediktritter.maven.plugin.development

class ConfigurationCacheFuncTest extends AbstractPluginFuncTest {
    def "supports configuration cache"() {
        given:
        javaMojo()
        buildFile << "mavenPlugin.helpMojoPackage.set('org.example.help')"

        and:
        run("build", "--configuration-cache")

        when:
        def result = run("build", "--configuration-cache")

        then:
        result.output.contains("Reusing configuration cache.")
    }
}
