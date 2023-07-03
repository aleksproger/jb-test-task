package dependency.manager.kit

import kotlinx.coroutines.*

class TopLevelDependencyTree(
    private val cache: DependencyTreeCache,
    private val downloader: DependencyDownloader,
    private val resolver: DependencyTreeResolver,
) {
    suspend fun construct(dependency: Dependency) = coroutineScope {
        val cachedDependencies = cache.get(dependency)?.let { it + dependency }

        if (cachedDependencies != null) {
            cachedDependencies.map { async { downloader.download(it) } }.awaitAll()
            println("Cached dependencies validation finished")
        } else {
            val resolved = resolver.resolve(dependency)
            cache.set(resolved, dependency)
        }
    }
}

class DependencyTreeResolver(
    private val dependenciesResolver: DependenciesResolver,
    private val versionPolicy: DependencyVersionPolicy
) {
    suspend fun resolve(dependency: Dependency): Set<Dependency>{
        return buildTree(dependenciesResolver.resolve(dependency), setOf())
    }

    private suspend fun buildTree(next: Set<Dependency>, visited: Set<Dependency>): Set<Dependency>  {
        if (next.isEmpty()) {
            return visited
        }
       
        val incompatibleWithCachedVersions = next.filter { 
            !versionPolicy.exist(it)
        }

        val children = coroutineScope {
            incompatibleWithCachedVersions.map { 
                async { dependenciesResolver.resolve(it) }
            }
        }

        return buildTree(children.awaitAll().flatten().toSet(), visited + incompatibleWithCachedVersions)
    }
}