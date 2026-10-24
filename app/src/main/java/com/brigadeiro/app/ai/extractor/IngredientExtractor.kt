package com.brigadeiro.app.ai.extractor

import com.brigadeiro.app.domain.model.ExtractionResult

/**
 * Turns free-form text into structured ingredient drafts. Implementations must
 * never persist anything; drafts always pass through the confirmation UI.
 */
interface IngredientExtractor {
    suspend fun extract(text: String): ExtractionResult
}
