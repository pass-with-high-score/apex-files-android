package app.pwhs.apexfilemanager.features.viewer.di

import app.pwhs.apexfilemanager.features.viewer.hex.HexViewerViewModel
import app.pwhs.apexfilemanager.features.viewer.image.ImageViewerViewModel
import app.pwhs.apexfilemanager.features.viewer.text.TextEditorViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewerModule = module {
    viewModelOf(::TextEditorViewModel)
    viewModelOf(::ImageViewerViewModel)
    viewModelOf(::HexViewerViewModel)
}
