package dependency.manager.kit

expect interface TransitiveDependenciesParser {
    suspend fun parse(pom: CachedArtifact): List<Dependency>
}

sealed class TransitiveDependenciesScope(val scopeName: String) {
    object Compile: TransitiveDependenciesScope("compile")
    object Test: TransitiveDependenciesScope("test")
    object Runtime: TransitiveDependenciesScope("runtime")
}