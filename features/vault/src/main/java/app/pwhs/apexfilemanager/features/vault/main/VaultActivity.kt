package app.pwhs.apexfilemanager.features.vault.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.features.vault.main.VaultUiAction
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

class VaultActivity : BaseActivity() {

    private val viewModel: VaultViewModel by viewModel()
    private var isNavigatingToExternal = false

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        isNavigatingToExternal = false
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                importUri(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VaultScreen(
                viewModel = viewModel,
                onNavigateBack = { finish() },
                onOpenFile = { file, mimeType -> openFile(file, mimeType) },
                onPickFiles = {
                    isNavigatingToExternal = true
                    filePickerLauncher.launch(arrayOf("*/*"))
                }
            )
        }
    }

    override fun onStop() {
        super.onStop()
        // Auto-lock when user leaves the app or switches tasks
        if (!isNavigatingToExternal && !isChangingConfigurations) {
            finish()
        }
    }

    private fun importUri(uri: Uri) {
        try {
            val fileName = getFileNameFromUri(uri) ?: "import_${System.currentTimeMillis()}"
            val tempFile = File(cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            viewModel.onAction(VaultUiAction.ImportFile(tempFile.absolutePath))
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể đọc tệp đã chọn", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = it.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path?.substringAfterLast('/')
        }
        return name
    }

    private fun openFile(file: File, mimeType: String) {
        try {
            isNavigatingToExternal = true
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            isNavigatingToExternal = false
            Toast.makeText(this, "Không tìm thấy ứng dụng phù hợp để mở tệp này", Toast.LENGTH_SHORT).show()
        }
    }
}
