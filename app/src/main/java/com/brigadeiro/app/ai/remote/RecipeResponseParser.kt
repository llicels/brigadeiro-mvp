package com.brigadeiro.app.ai.remote

import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.TextNormalizer
import com.brigadeiro.app.domain.model.Recipe
import com.brigadeiro.app.domain.model.RecipeIngredient
import com.brigadeiro.app.domain.model.RecipeStep
import com.brigadeiro.app.domain.model.RecipeSuggestion
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RecipeDto(
    val title: String? = null,
    val timeMinutes: Int? = null,
    val servings: Int? = null,
    val difficulty: String? = null,
    val imageUrl: String? = null,
    val availableIngredients: List<String> = emptyList(),
    val missingIngredients: List<String> = emptyList(),
    val ingredients: List<RecipeIngredientDto> = emptyList(),
    val steps: List<String> = emptyList(),
)

@Serializable
data class RecipeIngredientDto(val name: String? = null, val quantity: String? = null)

@Serializable
data class RecipeListDto(val recipes: List<RecipeDto> = emptyList())

class RecipeResponseParser(
    private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true },
) {
    fun parseList(raw: String): List<RecipeDto> {
        val payload = extractJson(raw) ?: return emptyList()
        return runCatching {
            json.decodeFromString<RecipeListDto>(payload).recipes
        }.getOrElse {
            runCatching { listOf(json.decodeFromString<RecipeDto>(payload)) }.getOrDefault(emptyList())
        }
    }

    fun parseOne(raw: String): RecipeDto? = parseList(raw).firstOrNull()

    private fun extractJson(raw: String): String? {
        val start = raw.indexOf('{')
        if (start < 0) return null
        var depth = 0
        for (i in start until raw.length) {
            when (raw[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return raw.substring(start, i + 1)
                }
            }
        }
        return null
    }
}

internal fun RecipeDto.toSuggestion(): RecipeSuggestion? {
    val name = title?.trim().orEmpty()
    if (name.isEmpty()) return null
    return RecipeSuggestion(
        id = "remote-" + name.lowercase().replace(Regex("[^a-z0-9]+"), "-"),
        title = name,
        timeMinutes = timeMinutes ?: 20,
        servings = servings ?: 2,
        difficulty = difficulty ?: "fácil",
        imageUrl = imageUrl?.trim()?.takeIf { it.isNotEmpty() },
        availableIngredients = availableIngredients,
        missingIngredients = missingIngredients,
    )
}

internal fun RecipeDto.toRecipe(
    id: String,
    inventory: List<Ingredient>,
): Recipe {
    val owned = inventory.map { TextNormalizer.normalize(it.name) }.toSet()
    return Recipe(
        id = id,
        title = title?.trim().orEmpty().ifEmpty { "Receita do Brigadeiro" },
        timeMinutes = timeMinutes ?: 20,
        servings = servings ?: 2,
        difficulty = difficulty ?: "fácil",
        imageUrl = imageUrl?.trim()?.takeIf { it.isNotEmpty() },
        ingredients = this.ingredients.map { dto ->
            val name = dto.name?.trim().orEmpty()
            val normalized = TextNormalizer.normalize(name)
            RecipeIngredient(
                name = name,
                quantity = dto.quantity?.trim()?.takeIf { it.isNotEmpty() },
                available = normalized.isNotEmpty() &&
                    owned.any { it == normalized || it.contains(normalized) },
            )
        }.filter { it.name.isNotEmpty() },
        steps = steps.mapIndexed { index, text -> RecipeStep(index + 1, text) },
    )
}
