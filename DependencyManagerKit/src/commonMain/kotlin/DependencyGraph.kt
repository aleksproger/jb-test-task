package dependency.manager.kit

import kotlinx.coroutines.*

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
        val cachedDependencies = cache.get(dependency)

        if (cachedDependencies != null) {
            async { dependenciesResolver.resolve(dependency) }
            cachedDependencies.map { async { downloader.download(it) } }
            cachedDependencies
        } else {
            val resolved = nodeGraph.construct(dependency)
            cache.set(resolved, dependency)
            resolved
        }
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

        val children: List<ResolvedDependency> = coroutineScope {
            incompatibleWithCachedVersions.map { 
                async { dependenciesResolver.resolve(it) }
            }
        }.awaitAll()

        val childrenDependencies = children.flatMap { it.dependencies }

        return buildGraph(childrenDependencies.toSet(), visited + children.toSet())
    }
}