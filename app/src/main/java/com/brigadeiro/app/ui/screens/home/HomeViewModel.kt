package com.brigadeiro.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brigadeiro.app.domain.TimeProvider
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.RecipeSuggestion
import com.brigadeiro.app.domain.repository.DietRepository
import com.brigadeiro.app.domain.repository.IngredientRepository
import com.brigadeiro.app.domain.usecase.SuggestRecipesUseCase
import com.brigadeiro.app.ui.util.greeting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val loading: Boolean = true,
    val greeting: String = "",
    val ingredientCount: Int = 0,
    val preview: List<Ingredient> = emptyList(),
    val suggestions: List<RecipeSuggestion> = emptyList(),
    val dietSummary: String = "",
)

class HomeViewModel(
    ingredientRepository: IngredientRepository,
    dietRepository: DietRepository,
    private val suggestRecipes: SuggestRecipesUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        ingredientRepository.observeAll(),
        dietRepository.profile,
    ) { ingredients, profile ->
        HomeUiState(
            loading = false,
            greeting = greeting(timeProvider.now()),
            ingredientCount = ingredients.size,
            preview = ingredients.take(6),
            suggestions = suggestRecipes(ingredients).take(8),
            dietSummary = profile.summary,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )
}
