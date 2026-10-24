package com.brigadeiro.app.domain

import com.brigadeiro.app.domain.model.DietProfile
import com.brigadeiro.app.domain.repository.DietRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeDietRepository(
    private var currentProfile: DietProfile = DietProfile(onboarded = true),
) : DietRepository {

    private val state = MutableStateFlow(currentProfile)

    override val profile: Flow<DietProfile> = state.asStateFlow()

    override suspend fun current(): DietProfile = state.value

    override suspend fun save(profile: DietProfile) {
        currentProfile = profile.copy(onboarded = true)
        state.value = currentProfile
    }
}
