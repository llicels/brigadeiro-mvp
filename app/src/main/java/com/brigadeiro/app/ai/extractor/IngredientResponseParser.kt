package com.brigadeiro.app.ai.extractor

import com.brigadeiro.app.domain.model.ExtractionResult
import com.brigadeiro.app.domain.model.IngredientDraft
import com.brigadeiro.app.domain.model.TextNormalizer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class IngredientDto(
    val name: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val confidence: Double? = null,
    val uncertain: Boolean? = null,
)

@Serializable
data class ExtractionDto(
    val ingredients: List<IngredientDto> = emptyList(),
)

/**
 * Safe parser for model output: it extracts the first JSON object, decodes it
 * leniently and then applies schema + domain validation. Invalid entries are
 * dropped rather than persisted. Used by the LLM extractor.
 */
class IngredientResponseParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    },
) {

    fun parse(raw: String, sourceText: String = ""): ExtractionResult {
        val payload = extractJsonObject(raw) ?: return ExtractionResult(emptyList(), sourceText)
        val dto = runCatching { json.decodeFromString<ExtractionDto>(payload) }.getOrNull()
            ?: return ExtractionResult(emptyList(), sourceText)

        val drafts = dto.ingredients.mapNotNull { toDraft(it) }
        return ExtractionResult(drafts, sourceText)
    }

    private fun toDraft(dto: IngredientDto): IngredientDraft? {
        val rawName = dto.name?.trim().orEmpty()
        if (rawName.length < 2) return null

        val normalized = TextNormalizer.normalize(rawName)
        if (normalized.length < 2) return null

        val quantity = dto.quantity?.takeIf { it.isFinite() && it >= 0.0 }
        val entry = IngredientLexicon.lookup(normalized)
        val confidence = (dto.confidence ?: 1.0).coerceIn(0.0, 1.0)

        return IngredientDraft(
            name = entry?.canonical ?: normalized,
            quantity = quantity,
            unit = dto.unit?.trim()?.takeIf { it.isNotEmpty() },
            category = entry?.category ?: com.brigadeiro.app.domain.model.IngredientCategory.Others,
            uncertain = dto.uncertain ?: (confidence < 0.6),
            confidence = confidence,
        )
    }

    /** Finds the first balanced `{...}` block in the text. */
    private fun extractJsonObject(raw: String): String? {
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
