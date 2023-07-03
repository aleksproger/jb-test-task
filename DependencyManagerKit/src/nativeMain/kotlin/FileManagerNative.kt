package dependency.manager.kit

import platform.Foundation.*
import kotlinx.cinterop.*

actual interface FileManager {
    actual fun fileExists(atPath: String): Boolean
    actual fun createDirectory(atPath: String, withIntermediateDirectories: Boolean)
    actual fun removeItem(atPath: String)
    actual fun copyItem(atPath: String, toPath: String)
    actual fun contentsOfDirectory(atPath: String, includeHiddenFiles: Boolean): List<String>
    actual fun saveToFile(atPath: String, content: String)
    actual fun readFromFile(atPath: String): String?
}

class FileManagerNative(private val subject: NSFileManager = NSFileManager.defaultManager()): FileManager {
    override fun fileExists(atPath: String): Boolean {
        val result = subject.fileExistsAtPath(atPath)
        return result
    }

    override fun createDirectory(atPath: String, withIntermediateDirectories: Boolean) {
        throwError { errorPointer ->
            subject.createDirectoryAtPath(atPath, withIntermediateDirectories, null, errorPointer)
        }
    }

    override fun removeItem(atPath: String) {
        throwError { errorPointer ->
            subject.removeItemAtPath(atPath, errorPointer)
        }
    }

    override fun copyItem(atPath: String, toPath: String) {
        throwError { errorPointer ->
            subject.moveItemAtPath(atPath, toPath, errorPointer)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun contentsOfDirectory(atPath: String, includeHiddenFiles: Boolean): List<String> {
        return throwError { errorPointer ->
            val contents = subject.contentsOfDirectoryAtPath(atPath, errorPointer) as List<String>
            
            if (includeHiddenFiles) {
                contents
            } else {
                contents.filter { !it.startsWith(".") }
            }
        }
    }

    // KT-30959: Incorrect warning about type-cast(https://youtrack.jetbrains.com/issue/KT-30959)
    override fun saveToFile(atPath: String, content: String) {
        throwError { errorPointer ->
            (content as NSString).writeToFile(atPath, true, NSUTF8StringEncoding, errorPointer)
        }
    }

    override fun readFromFile(atPath: String): String? {
        return throwError { errorPointer ->
            NSString.stringWithContentsOfFile(atPath, NSUTF8StringEncoding, errorPointer)
        }
    }
}

fun <T> throwError(block: (errorPointer: CPointer<ObjCObjectVar<NSError?>>) -> T): T {
    memScoped {
        val errorPointer: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr
        val result: T = block(errorPointer)
        val error: NSError? = errorPointer.pointed.value
        if (error != null) {
            throw NSErrorException(error)
        } else {
            return result
        }
    }
}

class NSErrorException(val nsError: NSError): Exception(nsError.toString())