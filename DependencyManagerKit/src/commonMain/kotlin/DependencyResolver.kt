package dependency.manager.kit

import kotlinx.coroutines.*


interface DependencyResolver {
    suspend fun resolve(dependency: Dependency): ResolvedDependency
}

class SingleDependencyResolver(
    private val artifactFetcher: ArtifactFetcher,
    private val transitiveDependenciesParser: TransitiveDependenciesParser,
): DependencyResolver {
    override suspend fun resolve(dependency: Dependency): ResolvedDependency = coroutineScope {
        //  ArtifactCachePersistent("/Users/alex/Developer/maven_cache", FileManagerNative()))
        val pom = async { artifactFetcher.fetch(Artifact(dependency.fullyQualifiedName(), ArtifactType.Pom)) }
        val jar = async { artifactFetcher.fetch(Artifact(dependency.fullyQualifiedName(), ArtifactType.Jar)) }
        var pomArtifact = pom.await()
        val dependencies = async { transitiveDependenciesParser.parse(pomArtifact) }
        ResolvedDependency(dependency.fullyQualifiedName(), dependencies.await(), pomArtifact, jar.await())
    }
}

class AnyExistingVersionDependencyResolver(
    private val subject: DependencyResolver,
    private val dependencyCache: DependencyCache,
    private val artifactCache: ArtifactCache
): DependencyResolver {
    override suspend fun resolve(dependency: Dependency): ResolvedDependency {
        val anyVersionDependency = dependencyCache.get(dependency, DependencyVersion.Any)
        
        if (anyVersionDependency != null) {
            val resolvedAnyVersionDependency = construct(anyVersionDependency)

            if (resolvedAnyVersionDependency != null) {
                return resolvedAnyVersionDependency
            } 
        }
        
        return subject.resolve(dependency)
    }

    private fun construct(dependency: Dependency): ResolvedDependency? {
        val pom = artifactCache.getCache(Artifact(dependency.fullyQualifiedName(), ArtifactType.Pom))
        val jar = artifactCache.getCache(Artifact(dependency.fullyQualifiedName(), ArtifactType.Jar))

        if (pom != null && jar != null) {
            return ResolvedDependency(dependency.fullyQualifiedName(), listOf(), pom, jar)
        }

        return null
    }
}

class DependencyTreeResolver(
    private val dependencyResolver: DependencyResolver
) {
    suspend fun resolve(dependency: Dependency): Node = coroutineScope {
        val resolvedDependency = async { dependencyResolver.resolve(dependency) }
        val root = async { buildTree(resolvedDependency.await()) }
        root.await()
    }

    private suspend fun buildTree(root: ResolvedDependency): Node = coroutineScope {
        val children = root.dependencies.map { 
            async {
                buildTree(dependencyResolver.resolve(it))
            }
        }

        Node(root, children.awaitAll())
    }
}

class Node(val dependency: ResolvedDependency, val children: List<Node>)