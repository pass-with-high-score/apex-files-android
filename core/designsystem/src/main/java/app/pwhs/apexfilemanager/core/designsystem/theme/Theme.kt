package app.pwhs.apexfilemanager.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import app.pwhs.apexfilemanager.core.storage.domain.model.AppSettings
import app.pwhs.apexfilemanager.core.storage.domain.model.ThemeMode
import app.pwhs.apexfilemanager.core.storage.domain.repository.SettingsRepository
import org.koin.core.context.GlobalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun ApexFileManagerTheme(
    darkTheme: Boolean? = null,
    dynamicColor: Boolean? = null,
    content: @Composable () -> Unit
) {
    val settingsRepo: SettingsRepository? = try {
        GlobalContext.get().getOrNull()
    } catch (_: Throwable) {
        null
    }

    val settings by (settingsRepo?.getSettings() ?: kotlinx.coroutines.flow.flowOf(AppSettings()))
        .collectAsState(initial = AppSettings())

    val isDark = darkTheme ?: when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val useDynamicColor = dynamicColor ?: settings.dynamicColor

    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
