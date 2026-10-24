package com.brigadeiro.app.ai.extractor

import com.brigadeiro.app.ai.AIProvider
import com.brigadeiro.app.ai.AIResult
import com.brigadeiro.app.domain.model.ExtractionResult

/**
 * LLM-backed extraction. It is only consulted when a provider is available;
 * the deterministic [HeuristicIngredientExtractor] remains the offline default.
 */
class LlmIngredientExtractor(
    private val provider: AIProvider,
    private val parser: IngredientResponseParser = IngredientResponseParser(),
) : IngredientExtractor {

    override suspend fun extract(text: String): ExtractionResult {
        if (!provider.isAvailable()) return ExtractionResult(emptyList(), text)
        return when (val result = provider.complete(text, SYSTEM_PROMPT)) {
            is AIResult.Success -> parser.parse(result.text, text)
            is AIResult.Failure -> ExtractionResult(emptyList(), text)
            AIResult.Unavailable -> ExtractionResult(emptyList(), text)
        }
    }

    companion object {
        val SYSTEM_PROMPT = """
            Você extrai ingredientes que o usuário diz ter em casa.
            Regras:
            1. Extraia SOMENTE ingredientes mencionados como disponíveis.
            2. Não invente ingredientes nem quantidades.
            3. Se a frase for um desejo/sugestão ("queria fazer..."), retorne lista vazia.
            4. Preserve a quantidade quando explicitamente dita.
            5. Responda apenas com JSON no schema:
            {"ingredients":[{"name":"tomate","quantity":2,"unit":"unidade","confidence":0.9}]}
        """.trimIndent()
    }
}
