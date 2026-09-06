package app.pwhs.apexfilemanager.features.wifishare.server

import android.content.Context
import android.os.Environment
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class KtorFileServer(
    private val context: Context
) {
    private var engine: CIOApplicationEngine? = null
    private val rootPath: String
        get() = Environment.getExternalStorageDirectory().absolutePath

    var lastError: Throwable? = null
        private set

    fun start(port: Int = 8080): Boolean {
        return try {
            lastError = null
            if (engine != null) {
                return true
            }

            val server = embeddedServer(CIO, port = port, host = "0.0.0.0") {
                install(CORS) {
                    anyHost()
                }

                routing {
                    get("/") {
                        val html = loadAssetHtml()
                        call.respondText(html, ContentType.Text.Html)
                    }

                    get("/api/files") {
                        val reqPath = call.parameters["path"] ?: "/"
                        val targetDir = resolveDirectory(reqPath)
                        val json = listDirectoryJson(targetDir)
                        call.respondText(json, ContentType.Application.Json)
                    }

                    get("/api/download") {
                        val reqPath = call.parameters["path"] ?: ""
                        val file = File(reqPath)
                        if (file.exists() && file.isFile) {
                            call.response.header(
                                HttpHeaders.ContentDisposition,
                                ContentDisposition.Attachment.withParameter(
                                    ContentDisposition.Parameters.FileName,
                                    file.name
                                ).toString()
                            )
                            call.respondFile(file)
                        } else {
                            call.respondText("File not found", status = HttpStatusCode.NotFound)
                        }
                    }

                    post("/api/upload") {
                        val reqPath = call.parameters["path"] ?: "/"
                        val targetDir = resolveDirectory(reqPath)
                        val multipart = call.receiveMultipart()
                        var savedCount = 0

                        multipart.forEachPart { part ->
                            if (part is PartData.FileItem) {
                                val fileName = part.originalFileName ?: "uploaded_file"
                                val destFile = File(targetDir, fileName)
                                part.streamProvider().use { input ->
                                    FileOutputStream(destFile).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                savedCount++
                            }
                            part.dispose()
                        }
                        call.respondText("""{"success": true, "uploaded": $savedCount}""", ContentType.Application.Json)
                    }
                }
            }

            server.start(wait = false)
            engine = server
            true
        } catch (e: Exception) {
            lastError = e
            engine = null
            false
        }
    }

    fun stop() {
        try {
            engine?.stop(500, 1000)
        } catch (_: Exception) {
        } finally {
            engine = null
        }
    }

    private fun resolveDirectory(path: String): File {
        val rawFile = if (path == "/" || path.isBlank()) {
            File(rootPath)
        } else {
            File(path)
        }
        val safeFile = if (rawFile.exists() && rawFile.isDirectory) rawFile else File(rootPath)
        return safeFile
    }

    private fun listDirectoryJson(dir: File): String {
        val isRoot = dir.absolutePath == rootPath || dir.parentFile == null
        val parentPath = if (isRoot) null else dir.parentFile?.absolutePath

        val files = dir.listFiles()?.sortedWith(
            compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() }
        ) ?: emptyList()

        val itemsJson = files.joinToString(separator = ",") { f ->
            val escapedName = escapeJson(f.name)
            val escapedPath = escapeJson(f.absolutePath)
            """{"name":"$escapedName","path":"$escapedPath","isDirectory":${f.isDirectory},"size":${f.length()},"lastModified":${f.lastModified()}}"""
        }

        val escapedCurrent = escapeJson(dir.absolutePath)
        val parentJsonValue = if (parentPath != null) "\"${escapeJson(parentPath)}\"" else "null"

        return """{"currentPath":"$escapedCurrent","parentPath":$parentJsonValue,"items":[$itemsJson]}"""
    }

    private fun escapeJson(value: String): String {
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun loadAssetHtml(): String {
        return try {
            context.assets.open("web/index.html").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            "<!DOCTYPE html><html><body><h1>Apex File Manager Wi-Fi Share</h1><p>Web UI asset not loaded.</p></body></html>"
        }
    }
}
