package app.pwhs.apexfilemanager.core.storage.domain.usecase

import app.pwhs.apexfilemanager.core.storage.domain.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

sealed interface BatchRenameRule {
    data class AddPrefixSuffix(val prefix: String, val suffix: String) : BatchRenameRule
    data class FindAndReplace(val find: String, val replace: String) : BatchRenameRule
    data class AutoNumbering(val baseName: String, val startIndex: Int = 1, val digits: Int = 3) : BatchRenameRule
}

data class RenamePreviewItem(
    val originalPath: String,
    val oldName: String,
    val newName: String
)

class BatchRenameUseCase(
    private val fileRepository: FileRepository
) {
    fun generatePreview(paths: List<String>, rule: BatchRenameRule): List<RenamePreviewItem> {
        return paths.mapIndexed { index, path ->
            val file = File(path)
            val oldName = file.name
            val nameWithoutExt = file.nameWithoutExtension
            val ext = if (file.extension.isNotEmpty()) ".${file.extension}" else ""

            val newName = when (rule) {
                is BatchRenameRule.AddPrefixSuffix -> {
                    "${rule.prefix}$nameWithoutExt${rule.suffix}$ext"
                }
                is BatchRenameRule.FindAndReplace -> {
                    if (rule.find.isEmpty()) oldName else oldName.replace(rule.find, rule.replace)
                }
                is BatchRenameRule.AutoNumbering -> {
                    val num = rule.startIndex + index
                    val formattedNum = num.toString().padStart(rule.digits, '0')
                    if (rule.baseName.isNotBlank()) {
                        "${rule.baseName}_$formattedNum$ext"
                    } else {
                        "${nameWithoutExt}_$formattedNum$ext"
                    }
                }
            }
            RenamePreviewItem(
                originalPath = path,
                oldName = oldName,
                newName = newName
            )
        }
    }

    suspend fun executeRename(items: List<RenamePreviewItem>): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            var successCount = 0
            for (item in items) {
                if (item.oldName != item.newName) {
                    val result = fileRepository.renameFile(item.originalPath, item.newName)
                    if (result.isSuccess) {
                        successCount++
                    }
                }
            }
            successCount
        }
    }
}
