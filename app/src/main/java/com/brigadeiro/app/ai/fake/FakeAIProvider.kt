package com.brigadeiro.app.ai.fake

import com.brigadeiro.app.ai.AIProvider
import com.brigadeiro.app.ai.AIResult

/**
 * Deterministic provider for development and tests. It is NEVER presented as a
 * real model; it simply echoes a fixed, well-formed response.
 */
class FakeAIProvider(
    private val response: String = DEFAULT_RESPONSE,
    private val available: Boolean = true,
) : AIProvider {

    override val name: String = "fake"

    override suspend fun isAvailable(): Boolean = available

    override suspend fun complete(prompt: String, systemPrompt: String?): AIResult {
        if (!available) return AIResult.Unavailable
        return AIResult.Success(response)
    }

    companion object {
        const val DEFAULT_RESPONSE =
            """{"ingredients":[{"name":"tomate","quantity":2,"unit":"unidade"},{"name":"arroz"},{"name":"queijo mussarela"}]}"""
    }
}
