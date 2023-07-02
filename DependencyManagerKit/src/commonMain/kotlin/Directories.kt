package dependency.manager.kit

object Directories {
    fun cacheDirectory(): String {
        return "/Users/alex/Developer/maven_cache"
    }

    fun dependencyDirectory(dependency: Dependency): String {
        return "${cacheDirectory()}/${dependency.groupId.replace(".", "/")}/${dependency.artifactId}"
    }

    fun dependencyDirectoryOfExactVersion(dependency: Dependency, version: String): String {
        return "${cacheDirectory()}/${dependency.groupId.replace(".", "/")}/${dependency.artifactId}/${version}"
    }
}