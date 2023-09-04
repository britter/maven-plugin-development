package de.benediktritter.maven.plugin.development.internal

import java.io.Serializable

data class Gavt(val groupId: String, val artifactId: String, val version: String, val type: String = "jar") : Serializable
