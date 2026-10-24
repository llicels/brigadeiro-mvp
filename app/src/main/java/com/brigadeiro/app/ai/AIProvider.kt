package com.brigadeiro.app.ai

/**
 * Result of a raw generation request. Providers never throw for expected
 * conditions: they return [Unavailable] or [Failure] so callers can degrade.
 */
sealed interface AIResult {
    data class Success(val text: String) : AIResult
    data class Failure(val reason: String, val cause: Throwable? = null) : AIResult
    data object Unavailable : AIResult
}

/**
 * A raw text-completion provider. The rest of the app talks to higher level
 * interfaces ([com.brigadeiro.app.ai.extractor.IngredientExtractor],
 * [com.brigadeiro.app.ai.RecipeGenerator]) and never needs to know which
 * concrete provider is active (fake, local or remote).
 */
interface AIProvider {
    val name: String

    /** Whether this provider can currently serve requests. */
    suspend fun isAvailable(): Boolean

    /**
     * Generates text for [prompt]. [systemPrompt] carries the task contract and
     * expected output schema.
     */
    suspend fun complete(prompt: String, systemPrompt: String? = null): AIResult
}
