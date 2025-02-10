package org.gradlex.maven.plugin.development.issues

import org.gradlex.maven.plugin.development.AbstractPluginFuncTest
import spock.lang.Issue

@Issue("https://github.com/gradlex-org/maven-plugin-development/issues/294")
class Issue294FuncTest extends AbstractPluginFuncTest {

    def "works in presence of #plugin plugin"() {
        given:
        buildFile.text = buildFile.text.replace("id 'java'", id)
        buildFile << """
            mavenPlugin.setHelpMojoPackage("com.example.help")
        """

        expect:
        run("build")

        where:
        plugin   | id
        'groovy' | "id 'groovy'"
        'kotlin' | "id 'org.jetbrains.kotlin.jvm' version '2.1.10'"
    }
}
