package dependency.manager.kit

import kotlinx.coroutines.*

interface ResolvedDependencyDownloader {
    suspend fun download(dependency: ResolvedDependency)
}

class ResolvedDependencyDownloaderDefault(private val artifactFetcher: ArtifactFetcher): ResolvedDependencyDownloader {
    override suspend fun download(dependency: ResolvedDependency) = coroutineScope {
        async { artifactFetcher.fetch(Artifact(dependency.fullyQualifiedName, ArtifactType.Pom)) }
        async { artifactFetcher.fetch(Artifact(dependency.fullyQualifiedName, dependency.artifactType)) }
        Unit
    }
}
