package de.benediktritter.maven.plugin.development.task

import org.codehaus.plexus.component.repository.ComponentDependency
import org.gradle.api.tasks.Input

data class DependencyDescriptor(
    @get:Input val groupId: String,
    @get:Input val artifactId: String,
    @get:Input val version: String,
    @get:Input val type: String?
) {
    fun toComponentDependency(): ComponentDependency = ComponentDependency().also {
        it.groupId = this.groupId
        it.artifactId = this.artifactId
        it.version = this.version
        it.type = this.type
    }
}