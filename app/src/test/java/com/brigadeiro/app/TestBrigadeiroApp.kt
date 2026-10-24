package com.brigadeiro.app

import android.app.Application
import androidx.room.Room
import com.brigadeiro.app.data.database.AppDatabase
import com.brigadeiro.app.domain.model.DietProfile
import com.brigadeiro.app.di.AppContainer
import com.brigadeiro.app.di.AppContainerProvider
import kotlinx.coroutines.runBlocking

/**
 * Test application wiring an in-memory database, mirroring the production graph.
 * Onboarding is marked complete so existing flows land straight on the app.
 */
class TestBrigadeiroApp : Application(), AppContainerProvider {

    override lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.inMemoryDatabaseBuilder(this, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        container = AppContainer(this, database)
        runBlocking {
            container.dietRepository.save(DietProfile(onboarded = true))
        }
    }
}
