package com.brigadeiro.app.audio

/**
 * Optional local Whisper.cpp backend.
 *
 * STATUS: not wired in this build. A faithful integration requires installing
 * the NDK, compiling whisper.cpp (or consuming a maintained prebuilt AAR) and
 * downloading a multilingual ggml model at runtime (never committed to Git).
 * Until that runtime is provided, this service reports itself unavailable so the
 * platform recognizer remains the active path. See AI.md.
 */
class WhisperSpeechToTextService(
    private val modelPath: () -> String? = { null },
) : SpeechToTextService {

    override val name: String = "whisper.cpp"

    override suspend fun isAvailable(): Boolean = !modelPath().isNullOrBlank()

    override suspend fun transcribe(audio: AudioInput): TranscriptionResult {
        val path = modelPath()
            ?: return TranscriptionResult.Failure(
                "Modelo Whisper não instalado.",
            )
        return TranscriptionResult.Failure(
            "Runtime Whisper não integrado nesta build (modelo: $path).",
        )
    }
}
