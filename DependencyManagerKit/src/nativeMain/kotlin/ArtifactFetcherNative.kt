package dependency.manager.kit

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.downloadTaskWithURL

actual interface ArtifactFetcher {
    actual suspend fun fetch(artifact: Artifact): CachedArtifact
}

@kotlinx.coroutines.ExperimentalCoroutinesApi
class ArtifactFetcherNew(
    private val repository: String,
    private val artifactCache: ArtifactCache
) {
    suspend fun fetch(artifact: Artifact): CachedArtifact? = suspendCancellableCoroutine<CachedArtifact?> { continuation ->
        val artifactURL = NSURL(string = "$repository/${artifact.mavenPath}")

        val task = NSURLSession
            .sharedSession()
            .downloadTaskWithURL(artifactURL) { location, response, error ->
                when {
                    error != null -> {
                        println("Finished load with error: ${artifact.fullyQualifiedName}")
                        continuation.resumeWith(Result.failure(Throwable("Error occurred during file download: ${error.localizedDescription}")))
                    }

                    response == null || (response as? NSHTTPURLResponse)?.statusCode == 404L -> {
                        continuation.resume(null) {
                            println("Canceled on resuming continuation: ${artifact.fullyQualifiedName}")
                        }
                    }

                    location != null -> {
                        location.path?.let { path ->
                            continuation.resume(artifactCache.cache(artifact, path)) {
                                println("Canceled on resuming continuation: ${artifact.fullyQualifiedName}")
                            }
                        }
                    }
                }
            }.also { it.resume() }

        continuation.invokeOnCancellation {
            println("Canceled load: ${artifact.fullyQualifiedName}")
            task.cancel()
        }
    }
} 

@kotlinx.coroutines.ExperimentalCoroutinesApi
class MultipleRepositoriesArtifactFetcher(
    private val repositories: List<String>,
    private val artifactCache: ArtifactCache
): ArtifactFetcher {
    override suspend fun fetch(artifact: Artifact): CachedArtifact {
        artifactCache.getCache(artifact)?.let {
            return it
        }

        for (repository in repositories) {
            ArtifactFetcherNew(repository, artifactCache).fetch(artifact)?.let {
                return it
            }
        }

        throw Throwable("Artifact not found: ${artifact.fullyQualifiedName}")
    }
}