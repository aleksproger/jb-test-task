package dependency.manager.kit

import platform.Foundation.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import platform.darwin.NSObject

actual interface PomManifestParser {
    actual suspend fun parse(pom: CachedArtifact): PomManifest
}

@kotlinx.coroutines.ExperimentalCoroutinesApi
class PomManifestParserNative private constructor(private val scopes: Set<String>): PomManifestParser {
    constructor(scopes: Set<TransitiveDependenciesScope>): this(scopes.map { it.scopeName }.toSet())

    override suspend fun parse(pom: CachedArtifact): PomManifest{
        return suspendCancellableCoroutine<PomManifest> { continuation ->
            var pomFileURL = NSURL(fileURLWithPath = pom.absolutePath)
            val xmlParser = XMLParser(NSXMLParser(pomFileURL), scopes)

            xmlParser.parse { manifest ->
                continuation.resume(manifest) {
                    println("Canceled parsing: $pom")
                }
            }
        }
    }
}

class XMLParser(
    private val parser: NSXMLParser,
    private val scopes: Set<String>
) {
    fun parse(callback: (PomManifest) -> Unit) {
        parser.delegate = XMLParserDelegate(callback, scopes)
        parser.parse()
    }
}

class XMLParserDelegate(
    private val onParsingFinished: (PomManifest) -> Unit,
    private val scopes: Set<String>
): NSObject(), NSXMLParserDelegateProtocol {
    private var dependencies: MutableSet<Dependency> = mutableSetOf()
    private var currentDependency: MutableMap<String, String>? = null
    private var isExclusion: Boolean = false
    private var isArtifactTypeResolving: Boolean = false
    private var artifactType: String? = null
    private var currentProperty: Pair<String, String?>? = null

    override fun parserDidEndDocument(parser: NSXMLParser) {
        val artifactType = when(artifactType) {
            "jar" -> ArtifactType.Jar
            "klib" -> ArtifactType.Klib
            else -> ArtifactType.Jar
        }
        onParsingFinished(PomManifest(artifactType, dependencies))
    }

    override fun parser(parser: NSXMLParser, foundCharacters: String) {
        setPropertyToDependency(foundCharacters)

        if (isArtifactTypeResolving) {
            setArtifactType(foundCharacters)
        }
    }
    
    override fun parser(
        parser: NSXMLParser,
        didStartElement: String,
        namespaceURI: String?,
        qualifiedName: String?,
        attributes: Map<Any?, *>
    ) {
        if (didStartElement == "exclusions") {
            finishDependency()
            isExclusion = true
            return
        }

        if (isExclusion) {
            return
        }

        if (didStartElement == "packaging") {
            isArtifactTypeResolving = true
        }

        if (didStartElement == "dependency") {
            currentDependency = mutableMapOf()
        }

        if (didStartElement == "groupId") {
            declarePropertyIfNeeded("groupId")
        }

        if (didStartElement == "artifactId") {
            declarePropertyIfNeeded("artifactId")
        }

        if (didStartElement == "version") {
            declarePropertyIfNeeded("version")
        }

        if (didStartElement == "scope") {
            declarePropertyIfNeeded("scope")
        }
    }

    override fun parser(
        parser: NSXMLParser,
        didEndElement: String,
        namespaceURI: String?,
        qualifiedName: String?
    ) {
        if (didEndElement == "exclusions") {
            isExclusion = false
            return
        }

        if (didEndElement == "dependency") {
            finishDependency()
        }
    }

    private fun declarePropertyIfNeeded(property: String) {
        if (currentDependency != null) {
            currentProperty = property to null
        }
    }

    private fun setPropertyToDependency(value: String) {
        currentProperty?.let {
            currentDependency?.put(it.first, value)
        }

        currentProperty = null
    }

    private fun setArtifactType(value: String) {
        artifactType = value
        isArtifactTypeResolving = false
    }

    private fun finishDependency() {
        currentDependency?.let {
            if (it["groupId"] != null && it["artifactId"] != null && it["version"] != null && scopes.contains(it["scope"])) {
                dependencies.add(Dependency("${it["groupId"]}:${it["artifactId"]}:${it["version"]}"))
            }
        }

        currentDependency = null
        currentProperty = null
    }
}