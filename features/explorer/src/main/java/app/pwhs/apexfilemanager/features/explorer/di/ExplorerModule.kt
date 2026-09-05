package app.pwhs.apexfilemanager.features.explorer.di

import app.pwhs.apexfilemanager.features.explorer.ExplorerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val explorerModule = module {
    viewModel { ExplorerViewModel(get()) }
}
