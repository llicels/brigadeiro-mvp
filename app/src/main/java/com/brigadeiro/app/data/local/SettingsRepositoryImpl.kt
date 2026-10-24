package com.brigadeiro.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.brigadeiro.app.domain.repository.AppConfig
import com.brigadeiro.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "brigadeiro_settings")

class SettingsRepositoryImpl(
    private val context: Context,
) : SettingsRepository {

    override val config: Flow<AppConfig> = context.dataStore.data.map { prefs ->
        AppConfig(
            remoteBaseUrl = prefs[KEY_BASE_URL].orEmpty(),
            remoteApiKey = prefs[KEY_API_KEY].orEmpty(),
            remoteModel = prefs[KEY_MODEL].orEmpty(),
        )
    }

    override suspend fun current(): AppConfig = config.first()

    override suspend fun saveRemote(baseUrl: String, apiKey: String, model: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BASE_URL] = baseUrl.trim()
            prefs[KEY_API_KEY] = apiKey.trim()
            prefs[KEY_MODEL] = model.trim()
        }
    }

    override suspend fun clearRemote() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_BASE_URL)
            prefs.remove(KEY_API_KEY)
            prefs.remove(KEY_MODEL)
        }
    }

    private companion object {
        val KEY_BASE_URL = stringPreferencesKey("remote_base_url")
        val KEY_API_KEY = stringPreferencesKey("remote_api_key")
        val KEY_MODEL = stringPreferencesKey("remote_model")
    }
}
