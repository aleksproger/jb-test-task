package dependency.manager.kit

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable


interface DependenciesResolver {
    suspend fun resolve(dependency: Dependency): ResolvedDependency
}

class PomDependenciesResolver(
    private val dependenciesParser: PomManifestParser,
    private val artifactFetcher: ArtifactFetcher
): DependenciesResolver {
    override suspend fun resolve(dependency: Dependency): ResolvedDependency = coroutineScope {
        println("Resolving $dependency")

        val manifest = artifactFetcher
            .fetch(Artifact(dependency.fullyQualifiedName(), ArtifactType.Pom))
            .run { dependenciesParser.parse(this) }

        when (manifest.artifactType) {
            ArtifactType.Jar -> async { artifactFetcher.fetch(Artifact(dependency.fullyQualifiedName(), ArtifactType.Jar)) }
            ArtifactType.Klib -> async { artifactFetcher.fetch(Artifact(dependency.fullyQualifiedName(), ArtifactType.Klib)) }
            ArtifactType.Pom -> println("Unexpected artifact type to resolve")
        }

        println("Resolved $dependency")
        ResolvedDependency(dependency.fullyQualifiedName(), manifest.artifactType, manifest.dependencies)
    }
}