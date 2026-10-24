package com.brigadeiro.app.ai.local

import com.brigadeiro.app.ai.AIProvider
import com.brigadeiro.app.ai.AIResult
import com.brigadeiro.app.data.local.ModelManager

/**
 * Hook for an on-device runtime (e.g. LiteRT-LM / MediaPipe GenAI). The actual
 * engine is injected so the runtime dependency stays out of the core modules.
 *
 * When the model is not installed — or no engine is wired — this provider reports
 * itself unavailable and the app falls back to the deterministic extractor.
 */
fun interface OnDeviceTextEngine {
    suspend fun generate(prompt: String, systemPrompt: String?): AIResult
}

class LocalAIProvider(
    private val modelManager: ModelManager,
    private val engine: OnDeviceTextEngine? = null,
) : AIProvider {

    override val name: String = "local"

    override suspend fun isAvailable(): Boolean =
        engine != null && modelManager.isExtractionModelInstalled()

    override suspend fun complete(prompt: String, systemPrompt: String?): AIResult {
        if (!modelManager.isExtractionModelInstalled()) {
            return AIResult.Unavailable
        }
        val activeEngine = engine
            ?: return AIResult.Failure("Runtime on-device não integrado nesta build.")
        return activeEngine.generate(prompt, systemPrompt)
    }
}
