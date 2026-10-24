package com.brigadeiro.app.domain.repository

import com.brigadeiro.app.domain.model.DietProfile
import kotlinx.coroutines.flow.Flow

interface DietRepository {
    val profile: Flow<DietProfile>
    suspend fun current(): DietProfile

    /** Persists the profile and marks the user as onboarded. */
    suspend fun save(profile: DietProfile)
}
