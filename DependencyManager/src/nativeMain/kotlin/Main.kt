package dependency.manager.macos

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.*
import dependency.manager.kit.*

@kotlinx.coroutines.ExperimentalCoroutinesApi
fun main(args: Array<String>) = runBlocking {
    println("Arguments: ${args.joinToString(", ")}")
    val fileManager = FileManagerNative()
    val artifactCache = ArtifactCachePersistent(Directories.cacheDirectory(), fileManager)
    val artifactFetcher = MultipleRepositoriesArtifactFetcher(
        listOf("https://repo1.maven.org/maven2", "https://jfrog.bintray.com/jfrog-jars"),
        artifactCache
    )
    val dependencyDownloader = DependencyDownloaderDefault(artifactFetcher)

    val strictVersionResolver = PomDependenciesResolver(
        dependencyDownloader,
        TransitiveDependenciesParserNative(setOf(TransitiveDependenciesScope.Compile, TransitiveDependenciesScope.Runtime)),
    )

    var argumentsParser = ArgumentsParser()
    val rootDependency = argumentsParser.parse(args) ?: Dependency("org.jfrog.buildinfo:build-info-extractor-gradle:4.2.0")

    val graph = TopLevelDependencyGraph(
        DependencyGraphCache(fileManager),
        dependencyDownloader,
        NodeDependencyGraph(
            strictVersionResolver,
            AnyExistingDependencyVersionPolicy(fileManager)
        )
    )

    graph.construct(rootDependency)
    println("Done resolving $rootDependency")
}

