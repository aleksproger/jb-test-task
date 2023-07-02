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

fun Dependency.versionAgnosticName(): String {
    return "$groupId:$artifactId"
}

fun Dependency.anyExistingVersion(): String {
    return "$groupId:$artifactId"
}

data class ResolvedDependency(val fullyQualifiedName: String, val dependencies: List<Dependency>) 
//, val pom: CachedArtifact, val jar: CachedArtifact)

fun ResolvedDependency.versionAgnosticName(): String {
    return "${fullyQualifiedName.split(":")[0]}:${fullyQualifiedName.split(":")[1]}"
}


sealed class DependencyVersion {
    object Any: DependencyVersion()
    data class Exact(val rawVersion: String): DependencyVersion()
}