package com.brigadeiro.app.ui.screens.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brigadeiro.app.domain.model.Recipe
import com.brigadeiro.app.domain.model.RecipeSuggestion
import com.brigadeiro.app.domain.repository.IngredientRepository
import com.brigadeiro.app.domain.usecase.GenerateRecipeUseCase
import com.brigadeiro.app.domain.usecase.SuggestRecipesUseCase
import com.brigadeiro.app.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeDetailUiState(
    val loading: Boolean = true,
    val recipe: Recipe? = null,
    val error: Boolean = false,
)

class RecipeDetailViewModel(
    private val ingredientRepository: IngredientRepository,
    private val suggestRecipes: SuggestRecipesUseCase,
    private val generateRecipe: GenerateRecipeUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val recipeId: String = savedStateHandle.get<String>(Routes.ARG_RECIPE_ID).orEmpty()

    private val _state = MutableStateFlow(RecipeDetailUiState())
    val state: StateFlow<RecipeDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            runCatching {
                val ingredients = ingredientRepository.getAll()
                val suggestion = suggestRecipes(ingredients).firstOrNull { it.id == recipeId }
                    ?: RecipeSuggestion(
                        id = recipeId,
                        title = "",
                        timeMinutes = 20,
                        servings = 2,
                        availableIngredients = emptyList(),
                        missingIngredients = emptyList(),
                    )
                generateRecipe(suggestion, ingredients)
            }.onSuccess { recipe ->
                _state.update { it.copy(loading = false, recipe = recipe, error = false) }
            }.onFailure {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }
}
