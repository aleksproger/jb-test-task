package dependency.manager.kit

expect interface FileManager {
    fun fileExists(atPath: String): Boolean
    fun createDirectory(atPath: String, withIntermediateDirectories: Boolean)
    fun removeItem(atPath: String)
    fun copyItem(atPath: String, toPath: String)
    fun contentsOfDirectory(atPath: String, includeHiddenFiles: Boolean): List<String>
    fun saveToFile(atPath: String, content: String)
    fun readFromFile(atPath: String): String?
}