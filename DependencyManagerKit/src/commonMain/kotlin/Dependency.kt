package dependency.manager.kit

import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class Dependency(val groupId: String, val artifactId: String, val version: String) {
    constructor(fullyQualifiedName: String) : this(
        fullyQualifiedName.split(":")[0],
        fullyQualifiedName.split(":")[1],
        fullyQualifiedName.split(":")[2]
    )
}

fun Dependency.fullyQualifiedName(): String {
    return "$groupId:$artifactId:$version"
}

@Serializable
data class ResolvedDependency(val fullyQualifiedName: String, val artifactType: ArtifactType, val dependencies: Set<Dependency>)

