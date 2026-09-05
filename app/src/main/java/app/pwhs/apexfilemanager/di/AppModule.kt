package app.pwhs.apexfilemanager.di

import app.pwhs.apexfilemanager.features.home.HomeViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { HomeViewModel(androidApplication(), get()) }
}
