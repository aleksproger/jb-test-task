package dependency.manager.kit

import kotlinx.coroutines.*
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

        val pom = artifactFetcher.fetch(Artifact(dependency.fullyQualifiedName(), ArtifactType.Pom))
        val manifest = dependenciesParser.parse(pom)

        if (manifest.artifactType == ArtifactType.Jar) {
            async { artifactFetcher.fetch(Artifact(dependency.fullyQualifiedName(), ArtifactType.Jar)) }
        } else if (manifest.artifactType == ArtifactType.Klib) {
            async { artifactFetcher.fetch(Artifact(dependency.fullyQualifiedName(), ArtifactType.Klib)) }
        }

        println("Resolved $dependency")
        ResolvedDependency(dependency.fullyQualifiedName(), manifest.artifactType, manifest.dependencies)
    }
}