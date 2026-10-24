package com.brigadeiro.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brigadeiro.app.domain.model.DietProfile
import com.brigadeiro.app.domain.repository.DietRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DietUiState(
    val profile: DietProfile = DietProfile(),
    val loading: Boolean = true,
    val saved: Boolean = false,
)

class DietViewModel(
    private val dietRepository: DietRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DietUiState())
    val state: StateFlow<DietUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(profile = dietRepository.current(), loading = false) }
        }
    }

    fun save(profile: DietProfile) {
        viewModelScope.launch {
            val saved = profile.copy(onboarded = true)
            dietRepository.save(saved)
            _state.update { it.copy(profile = saved, saved = true) }
        }
    }

    fun skip() {
        save(DietProfile(onboarded = true))
    }
}
