package dependency.manager.kit

import kotlinx.coroutines.*

interface DependencyDownloader {
    suspend fun download(dependency: Dependency): Pair<CachedArtifact, CachedArtifact>
}

class DependencyDownloaderDefault(private val artifactFetcher: ArtifactFetcher): DependencyDownloader {
    override suspend fun download(dependency: Dependency): Pair<CachedArtifact, CachedArtifact> = coroutineScope {
        val pom = async { artifactFetcher.fetch(Artifact(dependency.fullyQualifiedName(), ArtifactType.Pom)) }
        val jar = async { artifactFetcher.fetch(Artifact(dependency.fullyQualifiedName(), ArtifactType.Jar)) }
        Pair(pom.await(), jar.await())
    }
}
