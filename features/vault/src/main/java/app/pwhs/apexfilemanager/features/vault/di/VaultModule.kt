package app.pwhs.apexfilemanager.features.vault.di

import app.pwhs.apexfilemanager.features.vault.auth.VaultAuthViewModel
import app.pwhs.apexfilemanager.features.vault.main.VaultViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val vaultModule = module {
    viewModel { VaultAuthViewModel(get(), get(), get()) }
    viewModel { VaultViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
