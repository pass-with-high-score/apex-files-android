package app.pwhs.apexfilemanager.features.recents.di

import app.pwhs.apexfilemanager.features.recents.RecentsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val recentsModule = module {
    viewModelOf(::RecentsViewModel)
}
