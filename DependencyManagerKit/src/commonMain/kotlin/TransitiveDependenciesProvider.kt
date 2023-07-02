package dependency.manager.kit

import kotlinx.coroutines.*

interface TransitiveDependenciesProvider {
    suspend fun provide(pom: CachedArtifact): List<Dependency>
}

class AnyExistingTransitiveDependenciesProvider(
    private val dependenciesParser: TransitiveDependenciesParser,
    private val fileManager: FileManager,
): TransitiveDependenciesProvider {

    override suspend fun provide(pom: CachedArtifact): List<Dependency> {
        return dependenciesParser.parse(pom)
    }

    private fun tryResolveToExistingVersion(dependency: Dependency): Dependency? {
        val dependencyDirectory = Directories.dependencyDirectory(dependency)
        if (!fileManager.fileExists(dependencyDirectory)) {
            return null
        }

        val versions = fileManager.contentsOfDirectory(dependencyDirectory, false)
        if (versions.isNotEmpty()) {
            return Dependency(dependency.groupId, dependency.artifactId, versions.first())
        }

        return null
    }
}
