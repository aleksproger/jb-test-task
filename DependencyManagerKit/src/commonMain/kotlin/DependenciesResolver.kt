package dependency.manager.kit

import kotlinx.coroutines.*

interface DependenciesResolver {
    suspend fun resolve(dependency: Dependency): Set<Dependency>
}

class PomDependenciesResolver(
    private val downloader: DependencyDownloader,
    private val dependenciesParser: TransitiveDependenciesParser,
): DependenciesResolver {
    override suspend fun resolve(dependency: Dependency): Set<Dependency> = coroutineScope {
        println("Resolving $dependency")

        val (pom, _) = downloader.download(dependency)
        val dependencies = dependenciesParser.parse(pom)
        
        println("Resolved $dependency")
        dependencies
    }
}