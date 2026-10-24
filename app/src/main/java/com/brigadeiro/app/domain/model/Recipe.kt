package com.brigadeiro.app.domain.model

/** A lightweight suggestion shown on the Ideias screen. */
data class RecipeSuggestion(
    val id: String,
    val title: String,
    val timeMinutes: Int,
    val activeTimeMinutes: Int = timeMinutes,
    val servings: Int,
    val difficulty: String = "fácil",
    val estimatedCostBRL: Double = 0.0,
    val imageSlug: String = id,
    val imageEmoji: String = "\uD83C\uDF7D\uFE0F",
    val imageUrl: String? = null,
    val availableIngredients: List<String>,
    val missingIngredients: List<String>,
    val freezerFriendly: Boolean = false,
) {
    val matchScore: Int
        get() {
            val total = availableIngredients.size + missingIngredients.size
            if (total == 0) return 0
            return ((availableIngredients.size * 100) / total)
        }

    /** True when every recipe ingredient is already in the pantry. */
    val hasEverything: Boolean get() = missingIngredients.isEmpty()
}

data class RecipeIngredient(
    val name: String,
    val quantity: String? = null,
    val available: Boolean = false,
)

data class RecipeStep(
    val order: Int,
    val text: String,
)

/** A suggested alternative for an ingredient, shown as an option (never a rule). */
data class IngredientSubstitution(
    val ingredient: String,
    val alternatives: List<String>,
)

data class Recipe(
    val id: String,
    val title: String,
    val timeMinutes: Int,
    val activeTimeMinutes: Int = timeMinutes,
    val servings: Int,
    val difficulty: String,
    val estimatedCostBRL: Double = 0.0,
    val imageSlug: String = id,
    val imageEmoji: String = "\uD83C\uDF7D\uFE0F",
    val imageUrl: String? = null,
    val ingredients: List<RecipeIngredient>,
    val steps: List<RecipeStep>,
    val substitutions: List<IngredientSubstitution> = emptyList(),
    val requiredEquipment: List<Equipment> = emptyList(),
    val freezerFriendly: Boolean = false,
)
