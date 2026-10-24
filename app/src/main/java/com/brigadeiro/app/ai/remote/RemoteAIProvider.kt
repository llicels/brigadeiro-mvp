package com.brigadeiro.app.ai.remote

import com.brigadeiro.app.ai.AIProvider
import com.brigadeiro.app.ai.AIResult
import com.brigadeiro.app.domain.repository.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * OpenAI-compatible chat provider. Works with OpenAI, OpenRouter, Groq, a local
 * Ollama server, etc. It is only active when the user configures a base URL,
 * model and key in Settings; nothing is hardcoded and no request is sent until
 * then.
 */
class RemoteAIProvider(
    private val configProvider: suspend () -> AppConfig,
    private val client: OkHttpClient = defaultClient(),
) : AIProvider {

    override val name: String = "remote"

    override suspend fun isAvailable(): Boolean = configProvider().remoteConfigured

    override suspend fun complete(prompt: String, systemPrompt: String?): AIResult {
        val config = configProvider()
        if (!config.remoteConfigured) return AIResult.Unavailable

        val messages = buildList {
            if (!systemPrompt.isNullOrBlank()) add(ChatMessage("system", systemPrompt))
            add(ChatMessage("user", prompt))
        }
        val body = json.encodeToString(
            ChatRequest.serializer(),
            ChatRequest(model = config.remoteModel, messages = messages),
        )

        return withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url(endpoint(config.remoteBaseUrl))
                    .addHeader("Authorization", "Bearer ${config.remoteApiKey}")
                    .addHeader("Content-Type", "application/json")
                    .post(body.toRequestBody(JSON_MEDIA))
                    .build()

                client.newCall(request).execute().use { response ->
                    val text = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        AIResult.Failure("HTTP ${response.code}")
                    } else {
                        val parsed = runCatching {
                            json.decodeFromString(ChatResponse.serializer(), text)
                        }.getOrNull()
                        val content = parsed?.choices?.firstOrNull()?.message?.content
                        if (content.isNullOrBlank()) {
                            AIResult.Failure("Resposta vazia do provedor.")
                        } else {
                            AIResult.Success(content)
                        }
                    }
                }
            }.getOrElse { error ->
                AIResult.Failure("Falha de rede", error)
            }
        }
    }

    private fun endpoint(baseUrl: String): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        return if (trimmed.endsWith("/chat/completions")) trimmed else "$trimmed/chat/completions"
    }

    @Serializable
    private data class ChatMessage(val role: String, val content: String)

    @Serializable
    private data class ChatRequest(
        val model: String,
        val messages: List<ChatMessage>,
        val temperature: Double = 0.2,
    )

    @Serializable
    private data class ChatResponse(val choices: List<Choice> = emptyList())

    @Serializable
    private data class Choice(val message: ChatMessage? = null)

    companion object {
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
        private val json = Json { ignoreUnknownKeys = true }

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}
