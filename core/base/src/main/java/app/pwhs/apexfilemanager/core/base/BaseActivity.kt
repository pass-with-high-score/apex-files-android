package app.pwhs.apexfilemanager.core.base

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity

/**
 * Activity cơ sở kế thừa từ [FragmentActivity].
 * Đảm bảo mọi Activity trong hệ thống Multi-Activity đều được kích hoạt Edge-to-Edge tự động
 * và hỗ trợ các tính năng của FragmentActivity (như BiometricPrompt).
 */
abstract class BaseActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    /**
     * Tiện ích bọc setContent hỗ trợ thiết lập UI đồng bộ.
     */
    protected fun setBaseContent(content: @Composable () -> Unit) {
        setContent {
            content()
        }
    }
}
