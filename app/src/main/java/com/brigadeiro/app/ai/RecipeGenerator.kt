package com.brigadeiro.app.ai

import com.brigadeiro.app.domain.model.DietProfile
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.Recipe
import com.brigadeiro.app.domain.model.RecipeSuggestion

/**
 * Provider-agnostic recipe generation. The UI depends on this interface only.
 * Every implementation must respect the user's [DietProfile].
 */
interface RecipeGenerator {
    suspend fun suggestRecipes(
        ingredients: List<Ingredient>,
        profile: DietProfile,
        maxTotalMinutes: Int? = null,
    ): List<RecipeSuggestion>

    suspend fun generateRecipe(
        suggestion: RecipeSuggestion,
        ingredients: List<Ingredient>,
        profile: DietProfile,
    ): Recipe
}
