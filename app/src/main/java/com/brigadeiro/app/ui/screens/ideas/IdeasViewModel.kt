package com.brigadeiro.app.ui.screens.ideas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brigadeiro.app.domain.model.RecipeSuggestion
import com.brigadeiro.app.domain.repository.IngredientRepository
import com.brigadeiro.app.domain.usecase.SuggestRecipesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IdeasUiState(
    val loading: Boolean = true,
    val emptyKitchen: Boolean = false,
    val suggestions: List<RecipeSuggestion> = emptyList(),
    val maxMinutes: Int? = null,
    val error: Boolean = false,
)

class IdeasViewModel(
    private val ingredientRepository: IngredientRepository,
    private val suggestRecipes: SuggestRecipesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(IdeasUiState())
    val state: StateFlow<IdeasUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun setTimeFilter(maxMinutes: Int?) {
        if (_state.value.maxMinutes == maxMinutes) return
        _state.update { it.copy(maxMinutes = maxMinutes) }
        refresh()
    }

    fun refresh() {
        val maxMinutes = _state.value.maxMinutes
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            runCatching {
                val ingredients = ingredientRepository.getAll()
                if (ingredients.isEmpty()) {
                    _state.update {
                        it.copy(loading = false, emptyKitchen = true, suggestions = emptyList())
                    }
                } else {
                    val suggestions = suggestRecipes(ingredients, maxMinutes)
                    _state.update {
                        it.copy(
                            loading = false,
                            emptyKitchen = false,
                            suggestions = suggestions,
                            error = false,
                        )
                    }
                }
            }.onFailure {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }
}
