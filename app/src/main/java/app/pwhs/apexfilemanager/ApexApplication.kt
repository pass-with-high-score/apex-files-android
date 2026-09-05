package app.pwhs.apexfilemanager

import android.app.Application
import app.pwhs.apexfilemanager.core.storage.di.storageModule
import app.pwhs.apexfilemanager.di.appModule
import app.pwhs.apexfilemanager.features.archive.di.archiveModule
import app.pwhs.apexfilemanager.features.explorer.di.explorerModule
import app.pwhs.apexfilemanager.features.recents.di.recentsModule
import app.pwhs.apexfilemanager.features.search.di.searchModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ApexApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@ApexApplication)
            modules(
                storageModule,
                appModule,
                explorerModule,
                searchModule,
                archiveModule,
                recentsModule
            )
        }
    }
}
