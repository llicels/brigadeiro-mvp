package com.brigadeiro.app.domain.model

/**
 * Simple, fixed set of MVP categories. No smart auto-categorisation yet.
 */
enum class IngredientCategory(val label: String, val emoji: String) {
    Produce("Hortifruti", "\uD83E\uDD66"),
    Grains("Grãos", "\uD83C\uDF5A"),
    Dairy("Laticínios", "\uD83E\uDDC0"),
    Proteins("Proteínas", "\uD83C\uDF57"),
    Grocery("Mercearia", "\uD83E\uDD6B"),
    Others("Outros", "\uD83E\uDDFA"),
    ;

    companion object {
        fun fromLabel(label: String?): IngredientCategory =
            entries.firstOrNull { it.name == label } ?: Others
    }
}

data class Ingredient(
    val id: Long = 0,
    val name: String,
    val normalizedName: String,
    val quantity: Double? = null,
    val unit: String? = null,
    val category: IngredientCategory = IngredientCategory.Others,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
)
