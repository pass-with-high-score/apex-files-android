package app.pwhs.apexfilemanager.features.archive.di

import app.pwhs.apexfilemanager.features.archive.ArchiveViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val archiveModule = module {
    viewModelOf(::ArchiveViewModel)
}
