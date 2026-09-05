package app.pwhs.apexfilemanager.features.wifishare.di

import app.pwhs.apexfilemanager.features.wifishare.WifiShareViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val wifiShareModule = module {
    viewModelOf(::WifiShareViewModel)
}
