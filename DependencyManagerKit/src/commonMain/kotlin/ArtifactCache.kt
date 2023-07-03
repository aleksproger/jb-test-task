package dependency.manager.kit

interface ArtifactCache {
    fun getCache(artifact: Artifact): CachedArtifact?
    fun cache(artifact: Artifact, tempLocation: String): CachedArtifact
}

class ArtifactCachePersistent(
    private val cacheDirectory: String,
    private val fileManager: FileManager
): ArtifactCache {
    init {
        if (!fileManager.fileExists(cacheDirectory)) {
            fileManager.createDirectory(cacheDirectory, true)
        }
    }

    override fun getCache(artifact: Artifact): CachedArtifact? {
        if (fileManager.fileExists("$cacheDirectory/${artifact.relativePath}")) {
            println("Cache hit. ArtifactCachePersistent.getCache: $cacheDirectory/${artifact.relativePath}")
            return CachedArtifact(artifact, "$cacheDirectory/${artifact.relativePath}")
        } else {
            return null
        }
    }


    override fun cache(artifact: Artifact, tempLocation: String): CachedArtifact {
        println("Caching artifact: $artifact")
        var artifactAbsoluteDirectory = "$cacheDirectory/${artifact.relativeDirectory}"
        var artifactAbsolutePath = "$cacheDirectory/${artifact.relativePath}"

        if (fileManager.fileExists(artifactAbsolutePath)) {
            fileManager.removeItem(artifactAbsolutePath)
        }

        fileManager.createDirectory(artifactAbsoluteDirectory, true)
        fileManager.copyItem(tempLocation, artifactAbsolutePath)

        println("Cached artifact: $artifact")

        return CachedArtifact(artifact, artifactAbsolutePath)
    }
}