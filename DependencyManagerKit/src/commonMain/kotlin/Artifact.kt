package dependency.manager.kit

import kotlinx.serialization.Serializable

@Serializable
sealed class ArtifactType(val fileExtension: String) {
    @Serializable
    object Jar : ArtifactType("jar")

    @Serializable
    object Pom : ArtifactType("pom")

    @Serializable
    object Klib : ArtifactType("klib")
}

class Artifact(private val groupId: String, private val artifactId: String, private val version: String, private val type: ArtifactType) {
    constructor(fullyQualifiedName: String, type: ArtifactType) : this(
        fullyQualifiedName.split(":")[0],
        fullyQualifiedName.split(":")[1],
        fullyQualifiedName.split(":")[2],
        type
    )
    
    val fullyQualifiedName = "$groupId:$artifactId:$version"
    val mavenPath: String = "${groupId.replace(".", "/")}/${artifactId}/${version}/${artifactId}-${version}.${type.fileExtension}"

    var relativeDirectory = "${groupId.replace(".", "/")}/${artifactId}/${version}"
    val relativePath = when (type) {
        is ArtifactType.Pom -> "$relativeDirectory/pom.xml"
        is ArtifactType.Jar -> "$relativeDirectory/$artifactId.jar"
        is ArtifactType.Klib -> "$relativeDirectory/$artifactId.klib"
    }
}

data class CachedArtifact(val artifact: Artifact, val absolutePath: String)

//Unresolved reference: JvmInline
// @JvmInline
// value class POM(private val pom: CachedArtifact)
// @JvmInline
// value class JAR(private val pom: CachedArtifact)