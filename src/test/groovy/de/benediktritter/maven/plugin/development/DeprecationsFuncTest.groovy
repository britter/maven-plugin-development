package de.benediktritter.maven.plugin.development

class DeprecationsFuncTest extends AbstractPluginFuncTest {

    def "should not cause deprecations"() {
        given:
        javaMojo()

        expect:
        run("build", "--warning-mode=fail")
    }
}
