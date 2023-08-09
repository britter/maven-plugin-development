package de.benediktritter.maven.plugin.development.task

import org.gradle.api.tasks.Input

data class ProjectInfo(@Input val group: String, @Input val name: String, @Input val version: String)
