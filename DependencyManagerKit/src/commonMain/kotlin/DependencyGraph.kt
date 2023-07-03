package dependency.manager.kit

import kotlinx.coroutines.*

interface DependencyGraph {
    suspend fun construct(dependency: Dependency): Set<Dependency>
}

class TopLevelDependencyGraph(
    private val cache: DependencyGraphCache,
    private val downloader: DependencyDownloader,
    private val nodeGraph: DependencyGraph,
): DependencyGraph{
    override suspend fun construct(dependency: Dependency): Set<Dependency> = coroutineScope {
        val cachedDependencies = cache.get(dependency)?.let { it + dependency }

        if (cachedDependencies != null) {
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
    override suspend fun construct(dependency: Dependency): Set<Dependency>{
        return buildGraph(dependenciesResolver.resolve(dependency), setOf())
    }

    private suspend fun buildGraph(next: Set<Dependency>, visited: Set<Dependency>): Set<Dependency>  {
        if (next.isEmpty()) {
            return visited
        }
       
        val incompatibleWithCachedVersions = next.filter { 
            !versionPolicy.exist(it) || (!visited.contains(it))
        }

        val children = coroutineScope {
            incompatibleWithCachedVersions.map { 
                async { dependenciesResolver.resolve(it) }
            }
        }

        return buildGraph(children.awaitAll().flatten().toSet(), visited + incompatibleWithCachedVersions)
    }
}