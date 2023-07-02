package dependency.manager.kit

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class DependencyTreeCache(private val fileManager: FileManager) {
    fun set(dependencies: Set<Dependency>, root: Dependency) {
        val jsonString = Json.encodeToString(dependencies)
        fileManager.saveToFile(Directories.resolvedFile(root), jsonString)
    }

    fun get(root: Dependency): Set<Dependency>? {
        try {
            val jsonString = fileManager.readFromFile(Directories.resolvedFile(root))

            if (jsonString != null) {
                return Json.decodeFromString<Set<Dependency>>(jsonString)
            }
            
            return null
        } catch(e: Exception) {
            return null
        }
    }
}
