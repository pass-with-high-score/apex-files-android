package app.pwhs.apexfilemanager.features.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.pwhs.apexfilemanager.core.storage.data.repository.StorageRepositoryImpl
import app.pwhs.apexfilemanager.core.storage.domain.usecase.GetStorageVolumesUseCase

class HomeViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val repository = StorageRepositoryImpl(application.applicationContext)
            val useCase = GetStorageVolumesUseCase(repository)
            return HomeViewModel(application, useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
