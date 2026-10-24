package com.brigadeiro.app.domain.usecase

import com.brigadeiro.app.ai.RecipeGenerator
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.Recipe
import com.brigadeiro.app.domain.model.RecipeSuggestion
import com.brigadeiro.app.domain.repository.DietRepository

class SuggestRecipesUseCase(
    private val generator: RecipeGenerator,
    private val dietRepository: DietRepository,
) {
    suspend operator fun invoke(
        ingredients: List<Ingredient>,
        maxTotalMinutes: Int? = null,
    ): List<RecipeSuggestion> =
        generator.suggestRecipes(ingredients, dietRepository.current(), maxTotalMinutes)
}

class GenerateRecipeUseCase(
    private val generator: RecipeGenerator,
    private val dietRepository: DietRepository,
) {
    suspend operator fun invoke(
        suggestion: RecipeSuggestion,
        ingredients: List<Ingredient>,
    ): Recipe = generator.generateRecipe(suggestion, ingredients, dietRepository.current())
}
