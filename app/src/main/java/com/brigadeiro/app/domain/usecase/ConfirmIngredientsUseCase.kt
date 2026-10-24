package com.brigadeiro.app.domain.usecase

import com.brigadeiro.app.domain.TimeProvider
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.IngredientCategory
import com.brigadeiro.app.domain.model.IngredientDraft
import com.brigadeiro.app.domain.model.TextNormalizer
import com.brigadeiro.app.domain.repository.IngredientRepository

/**
 * Merges confirmed drafts into the inventory. Existing ingredients with the same
 * normalized name are updated (quantities are summed when both are present).
 */
class ConfirmIngredientsUseCase(
    private val repository: IngredientRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(drafts: List<IngredientDraft>): Int {
        if (drafts.isEmpty()) return 0
        val now = timeProvider.now()
        val existing = repository.getAll().associateBy { it.normalizedName }
        var inserted = 0

        drafts.forEach { draft ->
            val normalized = TextNormalizer.normalize(draft.name)
            if (normalized.isBlank()) return@forEach
            val current = existing[normalized]
            if (current == null) {
                repository.add(
                    Ingredient(
                        name = draft.name.trim(),
                        normalizedName = normalized,
                        quantity = draft.quantity,
                        unit = draft.unit,
                        category = draft.category,
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
                inserted++
            } else {
                repository.update(
                    current.copy(
                        name = current.name,
                        quantity = mergeQuantity(current.quantity, draft.quantity),
                        unit = current.unit ?: draft.unit,
                        category = if (current.category == IngredientCategory.Others) {
                            draft.category
                        } else {
                            current.category
                        },
                        updatedAt = now,
                    ),
                )
            }
        }
        return inserted
    }

    private fun mergeQuantity(a: Double?, b: Double?): Double? = when {
        a != null && b != null -> a + b
        a != null -> a
        else -> b
    }
}
