package dependency.manager.kit

expect interface ArtifactFetcher {
    suspend fun fetch(artifact: Artifact): CachedArtifact
}
