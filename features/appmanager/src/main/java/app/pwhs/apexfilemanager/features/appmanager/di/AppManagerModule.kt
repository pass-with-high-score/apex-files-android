package app.pwhs.apexfilemanager.features.appmanager.di

import app.pwhs.apexfilemanager.features.appmanager.detail.ApkDetailViewModel
import app.pwhs.apexfilemanager.features.appmanager.list.AppManagerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appManagerModule = module {
    viewModelOf(::ApkDetailViewModel)
    viewModelOf(::AppManagerViewModel)
}
