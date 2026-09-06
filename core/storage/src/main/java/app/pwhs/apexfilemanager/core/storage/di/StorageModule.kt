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
    single<app.pwhs.apexfilemanager.core.storage.domain.manager.PrivilegedManager> {
        app.pwhs.apexfilemanager.core.storage.data.manager.PrivilegedManagerImpl(androidContext())
    }
    single<StorageRepository> { StorageRepositoryImpl(androidContext(), get()) }
    single<FileRepository> { FileRepositoryImpl(androidContext(), get()) }
    single<app.pwhs.apexfilemanager.core.storage.domain.repository.ArchiveRepository> {
        app.pwhs.apexfilemanager.core.storage.data.repository.ArchiveRepositoryImpl()
    }

    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.GetPrivilegedStatusUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.CheckPrivilegedStatusUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.RequestRootAccessUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.RequestShizukuAccessUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.SwitchAccessModeUseCase(get()) }

    factory { GetStorageVolumesUseCase(get()) }
    factory { GetDirectoryContentsUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.CreateFolderUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.RenameFileUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.DeleteFilesUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.CopyFilesUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.MoveFilesUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.SearchFilesUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.ListArchiveEntriesUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.ExtractArchiveUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.CreateArchiveUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.GetRecentFilesUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.GetApkFilesUseCase(get()) }

    single<app.pwhs.apexfilemanager.core.storage.domain.repository.TrashRepository> {
        app.pwhs.apexfilemanager.core.storage.data.repository.TrashRepositoryImpl()
    }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.GetTrashItemsUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.MoveToTrashUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.RestoreTrashItemUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.DeleteTrashPermanentlyUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.EmptyTrashUseCase(get()) }

    single<app.pwhs.apexfilemanager.core.storage.domain.repository.CleanerRepository> {
        app.pwhs.apexfilemanager.core.storage.data.repository.CleanerRepositoryImpl()
    }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.AnalyzeStorageUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.GetLargeFilesUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.GetJunkFilesUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.CleanJunkFilesUseCase(get()) }

    single<app.pwhs.apexfilemanager.core.storage.domain.repository.AppRepository> {
        app.pwhs.apexfilemanager.core.storage.data.repository.AppRepositoryImpl(androidContext())
    }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.GetApkInfoUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.GetInstalledAppsUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.BackupAppUseCase(get()) }

    single<app.pwhs.apexfilemanager.core.storage.domain.repository.NetworkServerRepository> {
        app.pwhs.apexfilemanager.core.storage.data.repository.NetworkServerRepositoryImpl(androidContext())
    }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.GetNetworkServersUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.SaveNetworkServerUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.DeleteNetworkServerUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.TestNetworkConnectionUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.ListRemoteFilesUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.DownloadRemoteFileUseCase(get()) }

    single<app.pwhs.apexfilemanager.core.storage.domain.repository.VaultRepository> {
        app.pwhs.apexfilemanager.core.storage.data.repository.VaultRepositoryImpl(androidContext())
    }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.CheckVaultSetupUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.AuthenticateVaultUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.SetVaultPinUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.GetVaultItemsUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.ImportToVaultUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.ExportFromVaultUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.DeleteVaultItemUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.GetVaultDecryptedFileUseCase(get()) }

    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.CalculateChecksumUseCase() }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.BatchRenameUseCase(get()) }

    single<app.pwhs.apexfilemanager.core.storage.domain.repository.SettingsRepository> {
        app.pwhs.apexfilemanager.core.storage.data.repository.SettingsRepositoryImpl(androidContext())
    }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.GetSettingsUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.UpdateThemeModeUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.UpdateDynamicColorUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.UpdateShowHiddenFilesUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.UpdateShowFileExtensionsUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.ClearAppCacheUseCase(get()) }
    factory { app.pwhs.apexfilemanager.core.storage.domain.usecase.GetAppCacheSizeUseCase(get()) }
}

