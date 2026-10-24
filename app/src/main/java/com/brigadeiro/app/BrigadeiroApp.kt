package com.brigadeiro.app

import android.app.Application
import com.brigadeiro.app.di.AppContainer
import com.brigadeiro.app.di.AppContainerProvider

class BrigadeiroApp : Application(), AppContainerProvider {

    override lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
