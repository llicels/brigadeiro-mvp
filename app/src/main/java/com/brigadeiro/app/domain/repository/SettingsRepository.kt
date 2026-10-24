package com.brigadeiro.app.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * User-configurable remote/provider settings. Credentials live here (DataStore)
 * and are never hardcoded in the build.
 */
data class AppConfig(
    val remoteBaseUrl: String = "",
    val remoteApiKey: String = "",
    val remoteModel: String = "",
) {
    val remoteConfigured: Boolean
        get() = remoteBaseUrl.isNotBlank() && remoteApiKey.isNotBlank() && remoteModel.isNotBlank()
}

interface SettingsRepository {
    val config: Flow<AppConfig>
    suspend fun current(): AppConfig
    suspend fun saveRemote(baseUrl: String, apiKey: String, model: String)
    suspend fun clearRemote()
}
