package app.pwhs.apexfilemanager.features.settings.di

import app.pwhs.apexfilemanager.features.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        SettingsViewModel(
            getSettingsUseCase = get(),
            updateThemeModeUseCase = get(),
            updateDynamicColorUseCase = get(),
            updateShowHiddenFilesUseCase = get(),
            updateShowFileExtensionsUseCase = get(),
            clearAppCacheUseCase = get(),
            getAppCacheSizeUseCase = get(),
            getPrivilegedStatusUseCase = get()
        )
    }
}
