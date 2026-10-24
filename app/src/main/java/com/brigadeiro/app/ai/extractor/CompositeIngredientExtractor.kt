package com.brigadeiro.app.ai.extractor

import com.brigadeiro.app.domain.model.ExtractionResult

/**
 * Runs the deterministic extractor first and only falls back to the LLM when
 * the heuristic found nothing or flagged an uncertain item. This keeps the app
 * fast and offline while still benefiting from the model when it is installed.
 */
class CompositeIngredientExtractor(
    private val heuristic: IngredientExtractor,
    private val llm: IngredientExtractor? = null,
) : IngredientExtractor {

    override suspend fun extract(text: String): ExtractionResult {
        val base = heuristic.extract(text)
        if (llm == null) return base

        val needsHelp = base.isEmpty || base.drafts.any { it.uncertain }
        if (!needsHelp) return base

        val refined = llm.extract(text)
        return when {
            refined.isEmpty -> base
            base.isEmpty -> refined
            else -> merge(base, refined)
        }
    }

    private fun merge(base: ExtractionResult, refined: ExtractionResult): ExtractionResult {
        val byName = linkedMapOf<String, com.brigadeiro.app.domain.model.IngredientDraft>()
        base.drafts.forEach { byName[com.brigadeiro.app.domain.model.TextNormalizer.normalize(it.name)] = it }
        refined.drafts.forEach { draft ->
            val key = com.brigadeiro.app.domain.model.TextNormalizer.normalize(draft.name)
            if (key !in byName) byName[key] = draft
        }
        return ExtractionResult(byName.values.toList(), base.sourceText)
    }
}
