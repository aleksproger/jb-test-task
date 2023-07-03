package dependency.manager.kit

interface DependencyVersionPolicy {
    fun exist(dependency: Dependency): Boolean
}

// May easily be extended to support SemVer
class AnyExistingDependencyVersionPolicy(
    private val fileManager: FileManager
): DependencyVersionPolicy {
    override fun exist(dependency: Dependency): Boolean {
        val dependencyDirectory = Directories.dependencyDirectory(dependency)
        if (!fileManager.fileExists(dependencyDirectory)) {
            return false
        }

        val versions = fileManager.contentsOfDirectory(dependencyDirectory, false)
        if (versions.isNotEmpty()) {
            return true
        }

        return false
    }
}