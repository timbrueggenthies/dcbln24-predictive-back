package demo.dcbln.predictiveback

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import demo.dcbln.predictiveback.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DcnblnApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appModule)
            androidContext(this@DcnblnApp)
        }
    }

}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val useGallery = booleanPreferencesKey("use_gallery")
