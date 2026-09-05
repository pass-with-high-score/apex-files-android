package app.pwhs.apexfilemanager.features.cleaner.di

import app.pwhs.apexfilemanager.features.cleaner.analysis.CleanerViewModel
import app.pwhs.apexfilemanager.features.cleaner.trash.TrashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val cleanerModule = module {
    viewModelOf(::TrashViewModel)
    viewModelOf(::CleanerViewModel)
}
