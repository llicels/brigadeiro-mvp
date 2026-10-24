package com.brigadeiro.app.ai.remote

import com.brigadeiro.app.ai.AIProvider
import com.brigadeiro.app.ai.AIResult
import com.brigadeiro.app.ai.RecipeGenerator
import com.brigadeiro.app.domain.diet.DietClassifier
import com.brigadeiro.app.domain.diet.DietPolicy
import com.brigadeiro.app.domain.model.DietProfile
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.Recipe
import com.brigadeiro.app.domain.model.RecipeSuggestion

/**
 * Remote (OpenAI-compatible) recipe generation. It transparently falls back to
 * the offline generator whenever the provider is unconfigured, offline or
 * returns invalid data, so the UI always has something to show.
 *
 * The diet profile is sent in the prompt and the result is post-filtered, so a
 * hallucinated dish can never override the user's restrictions.
 */
class RemoteRecipeGenerator(
    private val provider: AIProvider,
    private val fallback: RecipeGenerator,
    private val parser: RecipeResponseParser = RecipeResponseParser(),
) : RecipeGenerator {

    override suspend fun suggestRecipes(
        ingredients: List<Ingredient>,
        profile: DietProfile,
        maxTotalMinutes: Int?,
    ): List<RecipeSuggestion> {
        if (!provider.isAvailable()) {
            return fallback.suggestRecipes(ingredients, profile, maxTotalMinutes)
        }
        val prompt = buildSuggestPrompt(ingredients, profile, maxTotalMinutes)
        return when (val result = provider.complete(prompt, systemPrompt(profile))) {
            is AIResult.Success -> {
                val parsed = parser.parseList(result.text)
                    .mapNotNull { it.toSuggestion() }
                    .filter { suggestion -> isAllowed(suggestion, profile) }
                    .filter { suggestion ->
                        maxTotalMinutes == null || suggestion.timeMinutes <= maxTotalMinutes
                    }
                parsed.ifEmpty { fallback.suggestRecipes(ingredients, profile, maxTotalMinutes) }
            }
            else -> fallback.suggestRecipes(ingredients, profile, maxTotalMinutes)
        }
    }

    override suspend fun generateRecipe(
        suggestion: RecipeSuggestion,
        ingredients: List<Ingredient>,
        profile: DietProfile,
    ): Recipe {
        if (!provider.isAvailable()) {
            return fallback.generateRecipe(suggestion, ingredients, profile)
        }
        val prompt = buildGeneratePrompt(suggestion, ingredients, profile)
        return when (val result = provider.complete(prompt, systemPrompt(profile))) {
            is AIResult.Success -> {
                val dto = parser.parseOne(result.text)
                dto?.toRecipe(suggestion.id, ingredients)
                    ?: fallback.generateRecipe(suggestion, ingredients, profile)
            }
            else -> fallback.generateRecipe(suggestion, ingredients, profile)
        }
    }

    private fun isAllowed(suggestion: RecipeSuggestion, profile: DietProfile): Boolean =
        DietPolicy.isAllowed(
            DietClassifier.tagsOf(
                suggestion.availableIngredients + suggestion.missingIngredients,
            ),
            profile,
        )

    private fun buildSuggestPrompt(
        ingredients: List<Ingredient>,
        profile: DietProfile,
        maxTotalMinutes: Int?,
    ): String {
        val list = ingredients.joinToString(", ") { it.name }
        val freezer = if (profile.preferFreezerFriendly) {
            " Priorize receitas que congelam bem."
        } else {
            ""
        }
        val time = if (maxTotalMinutes != null) {
            " Cada receita deve ficar pronta em até $maxTotalMinutes minutos."
        } else {
            ""
        }
        return "Ingredientes disponíveis: $list. " +
            "A pessoa é ${profile.dietType.label} e evita: ${DietPolicy.forbiddenDescription(profile)}. " +
            "Sugira até 5 receitas usando principalmente esses itens e respeitando a dieta.$freezer$time"
    }

    private fun buildGeneratePrompt(
        suggestion: RecipeSuggestion,
        ingredients: List<Ingredient>,
        profile: DietProfile,
    ): String {
        val list = ingredients.joinToString(", ") { it.name }
        return "Receita: ${suggestion.title}. Ingredientes disponíveis: $list. " +
            "Evite: ${DietPolicy.forbiddenDescription(profile)}. Gere a receita completa."
    }

    private fun systemPrompt(profile: DietProfile): String = """
        Você sugere receitas práticas com o que a pessoa tem em casa.
        Respeite rigorosamente a dieta: ${DietPolicy.forbiddenDescription(profile)} nunca podem aparecer.
        Responda apenas com JSON no schema:
        {"recipes":[{"title":"...","timeMinutes":20,"servings":2,"difficulty":"fácil",
        "availableIngredients":["..."],"missingIngredients":["..."],
        "ingredients":[{"name":"arroz","quantity":"1 xícara"}],
        "steps":["passo 1","passo 2"]}]}
    """.trimIndent()
}
