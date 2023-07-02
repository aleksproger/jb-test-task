package dependency.manager.kit

data class Dependency(val groupId: String, val artifactId: String, val version: String) {
    constructor(fullyQualifiedName: String) : this(
        fullyQualifiedName.split(":")[0],
        fullyQualifiedName.split(":")[1],
        fullyQualifiedName.split(":")[2]
    )
}

fun Dependency.fullyQualifiedName(): String {
    return "$groupId:$artifactId:$version"
}

data class ResolvedDependency(val fullyQualifiedName: String, val dependencies: List<Dependency>, val pom: CachedArtifact, val jar: CachedArtifact)


interface DependencyCache {
    fun get(dependency: Dependency, version: DependencyVersion): Dependency?
}

class DependencyCachePersistent(
    private val artifactCache: ArtifactCache,
    private val fileManager: FileManager,
): DependencyCache {
    override fun get(dependency: Dependency, version: DependencyVersion): Dependency? {
        return when (version) {
            is DependencyVersion.Any -> { 
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
            is DependencyVersion.Exact -> {
                if (fileManager.fileExists(Directories.dependencyDirectoryOfExactVersion(dependency, version.rawVersion))) {
                    return Dependency(dependency.groupId, dependency.artifactId, version.rawVersion)
                }

                return null
            }
        }
    }
}

sealed class DependencyVersion {
    object Any: DependencyVersion()
    data class Exact(val rawVersion: String): DependencyVersion()
}