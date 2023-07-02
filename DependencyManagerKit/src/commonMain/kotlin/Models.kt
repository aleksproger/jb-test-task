
package dependency.manager.kit

data class PomFile(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val dependencies: List<PomFileDependency>
)

data class PomFileDependency(
    val groupId: String,
    val artifactId: String,
    val version: String
)
