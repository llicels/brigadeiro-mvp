package com.brigadeiro.app.domain.model

import java.text.Normalizer
import java.util.Locale

/**
 * Deterministic ingredient-name normalisation. Used for merging, searching and
 * matching recipes. Kept small and testable on purpose.
 */
object TextNormalizer {

    private val pluralExceptions = setOf(
        "arroz", "feijao", "milho", "leite", "azeite", "sal", "acucar",
        "queijo", "presunto", "pao", "peixe", "frango", "ovo", "couve",
    )

    fun normalize(raw: String): String {
        val withoutAccents = stripAccents(raw.lowercase(Locale.ROOT)).trim()
        val cleaned = withoutAccents
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return singularize(cleaned)
    }

    fun stripAccents(value: String): String {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{Mn}+"), "")
    }

    private fun singularize(value: String): String {
        val words = value.split(" ").map { word ->
            when {
                word.length <= 3 -> word
                word in pluralExceptions -> word
                word.endsWith("oes") -> word.dropLast(3) + "ao"
                word.endsWith("aes") -> word.dropLast(3) + "ao"
                word.endsWith("eis") -> word.dropLast(3) + "el"
                word.endsWith("is") -> word.dropLast(2) + "il"
                word.endsWith("s") -> word.dropLast(1)
                else -> word
            }
        }
        return words.joinToString(" ")
    }
}
