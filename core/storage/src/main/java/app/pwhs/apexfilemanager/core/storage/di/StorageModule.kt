package app.pwhs.apexfilemanager.core.storage.di

import app.pwhs.apexfilemanager.core.storage.data.repository.FileRepositoryImpl
import app.pwhs.apexfilemanager.core.storage.data.repository.StorageRepositoryImpl
import app.pwhs.apexfilemanager.core.storage.domain.repository.FileRepository
import app.pwhs.apexfilemanager.core.storage.domain.repository.StorageRepository
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetDirectoryContentsUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetStorageVolumesUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val storageModule = module {
    single<StorageRepository> { StorageRepositoryImpl(androidContext()) }
    single<FileRepository> { FileRepositoryImpl() }

    factory { GetStorageVolumesUseCase(get()) }
    factory { GetDirectoryContentsUseCase(get()) }
}
