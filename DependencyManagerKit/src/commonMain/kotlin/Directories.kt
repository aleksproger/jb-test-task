package dependency.manager.kit

object Directories {
    fun cacheDirectory(): String {
        return "/Users/sapial/Developer/maven_cache"
    }

    fun resolvedFile(dependency: Dependency): String {
        return "${dependencyDirectoryOfExactVersion(dependency, dependency.version)}/Manifest.json"
    }

    fun dependencyDirectory(dependency: Dependency): String {
        return "${cacheDirectory()}/${dependency.groupId.replace(".", "/")}/${dependency.artifactId}"
    }

    fun dependencyDirectoryOfExactVersion(dependency: Dependency, version: String): String {
        return "${cacheDirectory()}/${dependency.groupId.replace(".", "/")}/${dependency.artifactId}/${version}"
    }
}