package dependency.manager.kit

import platform.Foundation.*
import kotlinx.coroutines.*

actual interface ArtifactFetcher {
    actual suspend fun fetch(artifact: Artifact): CachedArtifact
}

class ArtifactFetcherNative(
    private val repositoryURL: String,
    private val artifactCache: ArtifactCache
) : ArtifactFetcher {
    override suspend fun fetch(artifact: Artifact): CachedArtifact {
        return suspendCancellableCoroutine<CachedArtifact> { continuation ->
            val task = NSURLSession
                .sharedSession()
                .downloadTaskWithURL(NSURL(string = "$repositoryURL/${artifact.mavenPath}")) { location, _, error ->
                    println("ArtifactFetcherNative.fetch: $repositoryURL/${artifact.mavenPath}")
                    if (error != null) {
                        println("Finished load with error: ${artifact.fullyQualifiedName}}")
                        continuation.resumeWith(Result.failure(Throwable("Error occurred during file download: ${error.localizedDescription}")))
                    } else if (location != null) {
                        location.path?.let {
                            println("Finished load with success: $it")

                            continuation.resume(artifactCache.cache(artifact, it)) {
                                println("Canceled on resuming continuation: ${artifact.fullyQualifiedName}")
                            }
                        }
                    }
                }

            task.resume() 

            continuation.invokeOnCancellation {
                println("Canceled load: ${artifact.fullyQualifiedName}")
                task.cancel()
            }
        }
    }
}

class ArtifactFetcherNew(
    private val repository: String,
    private val artifactCache: ArtifactCache
) {
    suspend fun fetch(artifact: Artifact): CachedArtifact? = suspendCancellableCoroutine<CachedArtifact?> { continuation ->
        println("Will resolve URL for $repository/${artifact.mavenPath}")
        val artifactURL = NSURL(string = "$repository/${artifact.mavenPath}")
        println("Did resolve URL for $repository/${artifact.mavenPath}")

        val task = NSURLSession
            .sharedSession()
            .downloadTaskWithURL(artifactURL) { location, response, error ->
                if (error != null) {
                    // println("Finished load with error: ${artifact.fullyQualifiedName}}")
                    continuation.resumeWith(Result.failure(Throwable("Error occurred during file download: ${error.localizedDescription}")))
                } else if (response == null || "${(response as NSHTTPURLResponse).statusCode}" == "404") {
                    continuation.resume(null) {
                        println("Canceled on resuming continuation: ${artifact.fullyQualifiedName}")
                    }
                } else if (location != null) {
                    location.path?.let {
                        println("Finished load with success: $repository/${artifact.mavenPath}")

                        continuation.resume(artifactCache.cache(artifact, it)) {
                            println("Canceled on resuming continuation: ${artifact.fullyQualifiedName}")
                        }
                    }
                }
            }

        task.resume() 

        continuation.invokeOnCancellation {
            println("Canceled load: ${artifact.fullyQualifiedName}")
            task.cancel()
        }
    }
} 

class MultipleRepositoriesArtifactFetcher(
    private val repositories: List<String>,
    private val artifactCache: ArtifactCache
): ArtifactFetcher {
    override suspend fun fetch(artifact: Artifact): CachedArtifact {
        val possibleCachedArtifact = artifactCache.getCache(artifact)
        if (possibleCachedArtifact != null) {
            return possibleCachedArtifact
        }
        for (repository in repositories) {
            val cachedArtifact = ArtifactFetcherNew(repository, artifactCache).fetch(artifact)
            if (cachedArtifact != null) {
                return cachedArtifact
            }
        }

        throw Throwable("Artifact not found: ${artifact.fullyQualifiedName}")
    }
}