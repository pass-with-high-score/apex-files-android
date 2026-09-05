package app.pwhs.apexfilemanager.features.search.di

import app.pwhs.apexfilemanager.features.search.SearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val searchModule = module {
    viewModelOf(::SearchViewModel)
}
