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
        cacheDirectory
            .takeIf { !fileManager.fileExists(it) }
            ?.let { fileManager.createDirectory(it, true) }
    }
    override fun getCache(artifact: Artifact): CachedArtifact? {
        return artifact
            .takeIf { fileManager.fileExists("$cacheDirectory/${it.relativePath}") }
            ?.let { CachedArtifact(it, "$cacheDirectory/${it.relativePath}") }
    }

    override fun cache(artifact: Artifact, tempLocation: String): CachedArtifact {
        val artifactAbsoluteDirectory = "$cacheDirectory/${artifact.relativeDirectory}"
        val artifactAbsolutePath = "$cacheDirectory/${artifact.relativePath}"

        artifactAbsolutePath
            .takeIf { fileManager.fileExists(it) }
            ?.let { fileManager.removeItem(it) }

        fileManager.createDirectory(artifactAbsoluteDirectory, true)
        fileManager.copyItem(tempLocation, artifactAbsolutePath)

        return CachedArtifact(artifact, artifactAbsolutePath)
    }
}