package dependency.manager.kit

expect interface PomManifestParser {
    suspend fun parse(pom: CachedArtifact): PomManifest
}

data class PomManifest(val artifactType: ArtifactType, val dependencies: Set<Dependency>)

sealed class TransitiveDependenciesScope(val scopeName: String) {
    object Compile: TransitiveDependenciesScope("compile")
    object Test: TransitiveDependenciesScope("test")
    object Runtime: TransitiveDependenciesScope("runtime")
}