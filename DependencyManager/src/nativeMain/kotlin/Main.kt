package dependency.manager.macos

import kotlinx.coroutines.runBlocking
import dependency.manager.kit.*

fun main(args: Array<String>) {
    println("Hello World on MacOS!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    runBlocking {
        val fileManager = FileManagerNative()
        val artifactCache = ArtifactCachePersistent("/Users/alex/Developer/maven_cache", fileManager)

        val dependencyResolver = SingleDependencyResolver(
            MultipleRepositoriesArtifactFetcher(
                listOf("https://repo1.maven.org/maven2", "https://jfrog.bintray.com/jfrog-jars"),
                artifactCache
            ),
            TransitiveDependenciesParserNative(setOf(TransitiveDependenciesScope.Compile, TransitiveDependenciesScope.Runtime))
        )

        val anyVersionResolver = AnyExistingVersionDependencyResolver(
            dependencyResolver,
            DependencyCachePersistent(artifactCache, fileManager),
            artifactCache
        )

        var treeResolver = DependencyTreeResolver(anyVersionResolver)

        val resolvedDependency = treeResolver.resolve(Dependency("org.jfrog.buildinfo:build-info-extractor-gradle:4.23.4"))
        // val resolvedDependency = treeResolver.resolve(Dependency("org.jfrog.buildinfo:build-info-extractor-gradle:4.2.0"))
        println(resolvedDependency)
    }
}