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
        return fileManager.fileExists(dependencyDirectory) && fileManager.contentsOfDirectory(dependencyDirectory, false).isNotEmpty()
    }
}