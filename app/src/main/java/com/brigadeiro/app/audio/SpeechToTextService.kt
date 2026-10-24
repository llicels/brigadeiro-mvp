package com.brigadeiro.app.audio

/** Raw audio for engines that transcribe a complete buffer (e.g. Whisper). */
data class AudioInput(
    val samples: FloatArray,
    val sampleRate: Int = 16_000,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioInput) return false
        return sampleRate == other.sampleRate && samples.contentEquals(other.samples)
    }

    override fun hashCode(): Int = 31 * samples.contentHashCode() + sampleRate
}

sealed interface TranscriptionResult {
    data class Success(val text: String) : TranscriptionResult
    data class Failure(val message: String) : TranscriptionResult
}

/**
 * Engine-agnostic speech-to-text contract. The live MVP path uses the platform
 * recognizer (see [com.brigadeiro.app.ui.voice]); [WhisperSpeechToTextService] is
 * the optional local implementation behind this same interface.
 */
interface SpeechToTextService {
    val name: String
    suspend fun isAvailable(): Boolean
    suspend fun transcribe(audio: AudioInput): TranscriptionResult
}
