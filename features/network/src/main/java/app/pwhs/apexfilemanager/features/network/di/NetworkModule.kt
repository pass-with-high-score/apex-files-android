package app.pwhs.apexfilemanager.features.network.di

import app.pwhs.apexfilemanager.features.network.explorer.RemoteExplorerViewModel
import app.pwhs.apexfilemanager.features.network.list.NetworkServerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val networkModule = module {
    viewModelOf(::NetworkServerViewModel)
    viewModelOf(::RemoteExplorerViewModel)
}
