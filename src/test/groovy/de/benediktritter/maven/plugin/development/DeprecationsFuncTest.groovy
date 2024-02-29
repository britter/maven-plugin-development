package de.benediktritter.maven.plugin.development

import de.benediktritter.maven.plugin.development.AbstractPluginFuncTest

import org.gradle.testkit.runner.TaskOutcome

class DeprecationsFuncTest extends AbstractPluginFuncTest {

    def "should not cause deprecations"() {
        given:
        javaMojo()

        expect:
        run("build", "--warning-mode=fail")
    }
}
