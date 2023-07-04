package dependency.manager.kit

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class DependencyGraphCache(private val fileManager: FileManager) {
    fun set(dependencies: Set<ResolvedDependency>, root: Dependency) {
        val jsonString = Json.encodeToString(dependencies)
        fileManager.saveToFile(Directories.resolvedFile(root), jsonString)
    }

    fun get(root: Dependency): Set<ResolvedDependency>? {
        try {
            val jsonString = fileManager.readFromFile(Directories.resolvedFile(root))

            if (jsonString != null) {
                return Json.decodeFromString<Set<ResolvedDependency>>(jsonString)
            }
            
            return null
        } catch(e: Exception) {
            return null
        }
    }
}
