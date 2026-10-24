package com.brigadeiro.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brigadeiro.app.data.local.ModelManager
import com.brigadeiro.app.domain.model.Equipment
import com.brigadeiro.app.domain.repository.AppConfig
import com.brigadeiro.app.domain.repository.DietRepository
import com.brigadeiro.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val localModelInstalled: Boolean = false,
    val dietSummary: String = "",
    val equipment: Set<Equipment> = Equipment.defaults,
    val savedMessage: String? = null,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val dietRepository: DietRepository,
    private val modelManager: ModelManager,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(localModelInstalled = modelManager.isExtractionModelInstalled()),
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val config: AppConfig = settingsRepository.current()
            val diet = dietRepository.current()
            _state.update {
                it.copy(
                    baseUrl = config.remoteBaseUrl,
                    apiKey = config.remoteApiKey,
                    model = config.remoteModel,
                    localModelInstalled = modelManager.isExtractionModelInstalled(),
                    dietSummary = diet.summary,
                    equipment = diet.equipment,
                )
            }
        }
    }

    fun refreshDiet() {
        viewModelScope.launch {
            val diet = dietRepository.current()
            _state.update { it.copy(dietSummary = diet.summary, equipment = diet.equipment) }
        }
    }

    fun toggleEquipment(equipment: Equipment) {
        val next = _state.value.equipment.toMutableSet().apply {
            if (!add(equipment)) remove(equipment)
        }
        _state.update { it.copy(equipment = next) }
        viewModelScope.launch {
            val profile = dietRepository.current()
            dietRepository.save(profile.copy(equipment = next))
        }
    }

    fun updateBaseUrl(value: String) = _state.update { it.copy(baseUrl = value, savedMessage = null) }
    fun updateApiKey(value: String) = _state.update { it.copy(apiKey = value, savedMessage = null) }
    fun updateModel(value: String) = _state.update { it.copy(model = value, savedMessage = null) }

    fun save() {
        val current = _state.value
        viewModelScope.launch {
            settingsRepository.saveRemote(current.baseUrl, current.apiKey, current.model)
            _state.update { it.copy(savedMessage = "Configuração salva.") }
        }
    }

    fun clear() {
        viewModelScope.launch {
            settingsRepository.clearRemote()
            _state.update {
                it.copy(baseUrl = "", apiKey = "", model = "", savedMessage = "Configuração removida.")
            }
        }
    }
}
