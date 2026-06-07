package com.adn.adnapp

import android.app.Application
import com.adn.adnapp.core.di.appModule
import com.adn.adnapp.core.di.networkModule
import com.adn.adnapp.core.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AdnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AdnApplication)
            modules(appModule, networkModule, repositoryModule)
        }
    }
}
