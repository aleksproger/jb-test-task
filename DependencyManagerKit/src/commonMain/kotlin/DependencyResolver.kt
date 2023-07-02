package dependency.manager.kit

import kotlinx.coroutines.*
import dependency.manager.kit.versionAgnosticName


interface DependencyResolver {
    suspend fun resolve(dependency: Dependency): ResolvedDependency
}

class DependencyDownloader(
    private val artifactFetcher: ArtifactFetcher
) {
    suspend fun download(dependency: Dependency): CachedArtifact = coroutineScope {
        val pom = async { artifactFetcher.fetch(Artifact(dependency.fullyQualifiedName(), ArtifactType.Pom)) }
        val jar = async { artifactFetcher.fetch(Artifact(dependency.fullyQualifiedName(), ArtifactType.Jar)) }
        pom.await()
    }
}

class SingleDependencyResolver(
    private val downloader: DependencyDownloader,
    private val dependenciesProvider: TransitiveDependenciesProvider,
) {
    suspend fun resolve(dependency: Dependency): Set<Dependency> = coroutineScope {
        val pom = downloader.download(dependency)
        dependenciesProvider.provide(pom).toSet()
    }
}

class AnyExistingVersionDependencyResolver(
    private val subject: SingleDependencyResolver,
    private val fileManager: FileManager,
) {
    fun exist(dependency: Dependency): Boolean {
        val dependencyDirectory = Directories.dependencyDirectory(dependency)
        if (!fileManager.fileExists(dependencyDirectory)) {
            return false
        }

        val versions = fileManager.contentsOfDirectory(dependencyDirectory, false)
        if (versions.isNotEmpty()) {
            return true
        }

        return false
    }
}


class TopLevelDependencyTree(
    private val cache: DependencyTreeCache,
    private val downloader: DependencyDownloader,
    private val resolver: DependencyTreeResolver,
) {
    suspend fun construct(dependency: Dependency) = coroutineScope {
        val cachedDependencies = cache.get(dependency)
        println("cachedDependencies: ${cachedDependencies?.joinToString("\n")}")

        if (cachedDependencies != null) {
            
            cachedDependencies.map { async { downloader.download(it) } }.awaitAll()
            async { downloader.download(dependency) }.await()
            
        } else {
            val resolved = resolver.resolve(dependency)
            cache.set(resolved, dependency)
        }
    }
}


class DependencyTreeValidator(
    private val strictVersionResolver: DependencyResolver,
    private val artifactFetcher: ArtifactFetcher,
) {
    suspend fun validate(dependencies: List<Dependency>) = coroutineScope {
        dependencies.map { 
            async { strictVersionResolver.resolve(it) }
        }.awaitAll()
    }
}

class DependencyTreeResolver(
    private val strictVersionResolver: SingleDependencyResolver,
    private val anyExistingVersionResolver: AnyExistingVersionDependencyResolver
) {
    suspend fun resolve(dependency: Dependency): Set<Dependency>{
        return buildTree(strictVersionResolver.resolve(dependency), setOf())
    }

    private suspend fun buildTree(next: Set<Dependency>, visited: Set<Dependency>): Set<Dependency>  {
        if (next.isEmpty()) {
            return visited
        }
       
        val incompatibleWithCachedVersions = next.filter { 
            !anyExistingVersionResolver.exist(it)
        }

        val children = coroutineScope {
            incompatibleWithCachedVersions.map { 
                async { strictVersionResolver.resolve(it) }
            }
        }

        return buildTree(children.awaitAll().flatten().toSet(), visited + incompatibleWithCachedVersions)
    }
}

class Node(val dependency: ResolvedDependency, val children: List<Node>)
