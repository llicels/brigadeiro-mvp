package com.brigadeiro.app.data.local

import android.content.Context
import java.io.File

/**
 * Manages on-device model files. Model binaries are never committed to the
 * repository and are never bundled in the APK; the user (or a future download
 * flow) places them in the app's internal `models/` directory.
 *
 * When no model is present, [LocalAIProvider] reports itself unavailable and the
 * app keeps working with the deterministic extractor and offline recipe catalog.
 */
class ModelManager(context: Context) {

    private val modelDir: File = File(context.filesDir, "models")

    val extractionModel: File = File(modelDir, "gemma-3-1b-it-int4.task")

    fun isExtractionModelInstalled(): Boolean =
        extractionModel.exists() && extractionModel.length() > MIN_MODEL_BYTES

    fun modelDirectoryPath(): String = modelDir.absolutePath

    companion object {
        // A valid 1B int4 model is far larger than this; guards against partial files.
        private const val MIN_MODEL_BYTES = 50L * 1024 * 1024
    }
}
