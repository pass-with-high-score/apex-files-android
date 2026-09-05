package app.pwhs.apexfilemanager.features.explorer.di

import app.pwhs.apexfilemanager.features.explorer.ExplorerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val explorerModule = module {
    viewModelOf(::ExplorerViewModel)
}
