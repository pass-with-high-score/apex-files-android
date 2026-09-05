package app.pwhs.apexfilemanager.core.storage.data.repository

import android.os.Environment
import app.pwhs.apexfilemanager.core.storage.domain.model.TrashItem
import app.pwhs.apexfilemanager.core.storage.domain.repository.TrashRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class TrashRepositoryImpl : TrashRepository {

    private val mutex = Mutex()

    private val trashDir: File
        get() {
            val root = Environment.getExternalStorageDirectory()
            val dir = File(root, ".apex_trash")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    private val filesDir: File
        get() {
            val dir = File(trashDir, "files")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    private val metadataFile: File
        get() = File(trashDir, "trash_metadata.json")

    override fun getTrashItems(): Flow<List<TrashItem>> = flow {
        val items = readMetadata()
        emit(items.sortedByDescending { it.deletedTimestamp })
    }.flowOn(Dispatchers.IO)

    override suspend fun moveToTrash(paths: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                val currentItems = readMetadata().toMutableList()
                for (path in paths) {
                    val file = File(path)
                    if (!file.exists()) continue

                    val id = UUID.randomUUID().toString()
                    val targetFile = File(filesDir, id)

                    val moved = file.renameTo(targetFile) || copyAndDelete(file, targetFile)
                    if (moved) {
                        currentItems.add(
                            TrashItem(
                                id = id,
                                name = file.name,
                                originalPath = file.absolutePath,
                                trashPath = targetFile.absolutePath,
                                sizeBytes = if (targetFile.isDirectory) targetFile.walkTopDown().sumOf { it.length() } else targetFile.length(),
                                deletedTimestamp = System.currentTimeMillis(),
                                isDirectory = targetFile.isDirectory
                            )
                        )
                    }
                }
                writeMetadata(currentItems)
            }
        }
    }

    override suspend fun restoreItem(trashId: String): Result<Unit> = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                val items = readMetadata().toMutableList()
                val item = items.find { it.id == trashId }
                    ?: throw IllegalArgumentException("Không tìm thấy tệp trong thùng rác")

                val trashFile = File(item.trashPath)
                val targetFile = File(item.originalPath)

                targetFile.parentFile?.mkdirs()
                val restored = trashFile.renameTo(targetFile) || copyAndDelete(trashFile, targetFile)
                if (!restored) {
                    throw IllegalStateException("Không thể phục hồi tệp về ${item.originalPath}")
                }

                items.removeAll { it.id == trashId }
                writeMetadata(items)
            }
        }
    }

    override suspend fun deletePermanently(trashId: String): Result<Unit> = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                val items = readMetadata().toMutableList()
                val item = items.find { it.id == trashId }
                if (item != null) {
                    val file = File(item.trashPath)
                    file.deleteRecursively()
                    items.removeAll { it.id == trashId }
                    writeMetadata(items)
                }
            }
        }
    }

    override suspend fun emptyTrash(): Result<Unit> = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                filesDir.deleteRecursively()
                filesDir.mkdirs()
                writeMetadata(emptyList())
            }
        }
    }

    private fun readMetadata(): List<TrashItem> {
        val file = metadataFile
        if (!file.exists()) return emptyList()
        return try {
            val jsonStr = file.readText(Charsets.UTF_8)
            val array = JSONArray(jsonStr)
            val list = mutableListOf<TrashItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TrashItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        originalPath = obj.getString("originalPath"),
                        trashPath = obj.getString("trashPath"),
                        sizeBytes = obj.optLong("sizeBytes", 0L),
                        deletedTimestamp = obj.optLong("deletedTimestamp", 0L),
                        isDirectory = obj.optBoolean("isDirectory", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun writeMetadata(items: List<TrashItem>) {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("originalPath", item.originalPath)
                put("trashPath", item.trashPath)
                put("sizeBytes", item.sizeBytes)
                put("deletedTimestamp", item.deletedTimestamp)
                put("isDirectory", item.isDirectory)
            }
            array.put(obj)
        }
        metadataFile.writeText(array.toString(), Charsets.UTF_8)
    }

    private fun copyAndDelete(src: File, dst: File): Boolean {
        return try {
            if (src.isDirectory) {
                src.copyRecursively(dst, overwrite = true) && src.deleteRecursively()
            } else {
                src.copyTo(dst, overwrite = true)
                src.delete()
            }
        } catch (_: Exception) {
            false
        }
    }
}
