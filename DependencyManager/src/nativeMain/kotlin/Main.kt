package dependency.manager.macos

import kotlinx.coroutines.runBlocking
import dependency.manager.kit.*

@kotlinx.coroutines.ExperimentalCoroutinesApi
fun main(args: Array<String>) {
    // println("Hello World on MacOS!")

    // // Try adding program arguments via Run/Debug configuration.
    // // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    runBlocking {
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

        val rootDependency = Dependency("org.jfrog.buildinfo:build-info-extractor-gradle:4.23.4")

        val topLevelTree = TopLevelDependencyTree(
            DependencyTreeCache(fileManager),
            dependencyDownloader,
            DependencyTreeResolver(
                strictVersionResolver,
                AnyExistingDependencyVersionPolicy(fileManager)
            )
        )

        topLevelTree.construct(rootDependency)
    }
}

