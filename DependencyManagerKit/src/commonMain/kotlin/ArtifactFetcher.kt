package dependency.manager.kit

sealed class ArtifactType(val fileExtension: String) {
    object Jar : ArtifactType("jar")
    object Pom : ArtifactType("pom")
}

class Artifact(val groupId: String, val artifactId: String, val version: String, val type: ArtifactType) {
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
    }
}

data class CachedArtifact(val artifact: Artifact, val absolutePath: String)

expect interface ArtifactFetcher {
    suspend fun fetch(artifact: Artifact): CachedArtifact
}
