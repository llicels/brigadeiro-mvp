package com.brigadeiro.app.domain.usecase

import com.brigadeiro.app.ai.extractor.IngredientExtractor
import com.brigadeiro.app.domain.model.ExtractionResult

class ParseIngredientsUseCase(
    private val extractor: IngredientExtractor,
) {
    suspend operator fun invoke(text: String): ExtractionResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ExtractionResult(emptyList(), text)
        return extractor.extract(trimmed)
    }
}
