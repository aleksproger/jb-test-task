package dependency.manager.kit

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

interface DependencyGraph {
    suspend fun construct(dependency: Dependency): Set<ResolvedDependency>
}

class TopLevelDependencyGraph(
    private val cache: DependencyGraphCache,
    private val downloader: ResolvedDependencyDownloader,
    private val nodeGraph: DependencyGraph,
    private var dependenciesResolver: DependenciesResolver
) {
    suspend fun construct(dependency: Dependency): Set<ResolvedDependency> = coroutineScope {
        cache.get(dependency)?.let {
            async { dependenciesResolver.resolve(dependency) }
            it.map { async { downloader.download(it) } }
            return@coroutineScope it
        }

        nodeGraph.construct(dependency).also { cache.set(it, dependency) }
    }
}

class NodeDependencyGraph(
    private val dependenciesResolver: DependenciesResolver,
    private val versionPolicy: DependencyVersionPolicy
): DependencyGraph {
    override suspend fun construct(dependency: Dependency): Set<ResolvedDependency>{
        return buildGraph(dependenciesResolver.resolve(dependency).dependencies, setOf())
    }

    private suspend fun buildGraph(next: Set<Dependency>, visited: Set<ResolvedDependency>): Set<ResolvedDependency>  {
        if (next.isEmpty()) {
            return visited
        }
       
        val incompatibleWithCachedVersions = next.filter { nextDependency ->
            !versionPolicy.exist(nextDependency) || !visited.any { it.fullyQualifiedName == nextDependency.fullyQualifiedName() }
        }

        val dependencies = coroutineScope {
            incompatibleWithCachedVersions.mapTo(mutableSetOf()) { async { dependenciesResolver.resolve(it) } }
        }.awaitAll()

        val transitiveDependencies = dependencies.flatMapTo(mutableSetOf()) { it.dependencies }

        return buildGraph(transitiveDependencies, visited + dependencies)
    }
}