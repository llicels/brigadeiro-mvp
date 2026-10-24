package com.brigadeiro.app.ai.extractor

import com.brigadeiro.app.domain.model.ExtractionResult
import com.brigadeiro.app.domain.model.IngredientDraft
import com.brigadeiro.app.domain.model.TextNormalizer

/**
 * Deterministic PT-BR extractor. Runs fully offline and handles the common
 * shapes of the MVP: "tenho dois tomates, arroz e queijo".
 *
 * It deliberately distinguishes possession ("tenho ...") from desire
 * ("queria fazer ...") so a suggestion is never registered as inventory.
 */
class HeuristicIngredientExtractor : IngredientExtractor {

    override suspend fun extract(text: String): ExtractionResult {
        val normalized = TextNormalizer.stripAccents(text.lowercase()).trim()
        if (normalized.isEmpty()) return ExtractionResult(emptyList(), text)

        val hasPossession = POSSESSION_MARKERS.any { normalized.contains(it) }
        val hasSuggestion = SUGGESTION_MARKERS.any { normalized.contains(it) }

        // A pure suggestion/craving is not an inventory statement.
        if (hasSuggestion && !hasPossession) {
            return ExtractionResult(emptyList(), text)
        }

        val drafts = linkedMapOf<String, IngredientDraft>()
        for (segment in splitSegments(normalized)) {
            val draft = parseSegment(segment) ?: continue
            val key = TextNormalizer.normalize(draft.name)
            if (key.isEmpty()) continue
            val existing = drafts[key]
            drafts[key] = if (existing == null) draft else merge(existing, draft)
        }

        return ExtractionResult(drafts.values.toList(), text)
    }

    private fun splitSegments(text: String): List<String> =
        text.split(Regex(",|;|\\.|\\n|\\s+e\\s+|\\s+com\\s+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    private fun parseSegment(rawSegment: String): IngredientDraft? {
        var segment = " " + rawSegment + " "
        segment = segment
            .replace(" um pouco de ", " __little__ ")
            .replace(" um pouco ", " __little__ ")
            .replace(" pouco de ", " __little__ ")

        var tokens = segment.trim().split(Regex("\\s+")).toMutableList()
        tokens = tokens.dropWhile { it in LEADING_FILLER }.toMutableList()
        if (tokens.isEmpty()) return null

        var quantity: Double? = null
        var unit: String? = null
        var nameTokens = mutableListOf<String>()
        var index = 0

        // Handle "500g" / "2kg" style tokens.
        val compact = COMPACT_QUANTITY.matchEntire(tokens.first())
        if (compact != null) {
            quantity = compact.groupValues[1].replace(',', '.').toDoubleOrNull()
            unit = normalizeUnit(compact.groupValues[2])
            index = 1
        } else if (tokens.first() == LITTLE) {
            index = 1
        } else {
            val parsed = parseNumber(tokens.first())
            if (parsed != null) {
                quantity = parsed
                index = 1
                if (index < tokens.size) {
                    val maybeUnit = normalizeUnit(tokens[index])
                    if (maybeUnit != null) {
                        unit = maybeUnit
                        index++
                    }
                }
            } else {
                val maybeUnit = normalizeUnit(tokens.first())
                if (maybeUnit != null) {
                    unit = maybeUnit
                    index = 1
                }
            }
        }

        nameTokens = tokens.drop(index).toMutableList()
        nameTokens = nameTokens.filterNot { it == LITTLE || it == "_" }.toMutableList()
        nameTokens = nameTokens.map { it.trim('?', '.', '!', ':') }.filter { it.isNotEmpty() }.toMutableList()
        while (nameTokens.isNotEmpty() && nameTokens.first() in ARTICLE_PREFIX) {
            nameTokens.removeAt(0)
        }

        val rawName = nameTokens.joinToString(" ").trim()
        if (rawName.isEmpty() || rawName.length < 2) return null

        val normalizedName = TextNormalizer.normalize(rawName)
        val entry = IngredientLexicon.lookup(normalizedName)
        val canonicalName = entry?.canonical ?: normalizedName

        val uncertain = normalizedName.length < 3 || nameTokens.size > 5
        val confidence = if (entry != null) 0.95 else 0.7

        return IngredientDraft(
            name = canonicalName,
            quantity = quantity,
            unit = unit,
            category = entry?.category ?: com.brigadeiro.app.domain.model.IngredientCategory.Others,
            uncertain = uncertain,
            confidence = confidence,
        )
    }

    private fun merge(a: IngredientDraft, b: IngredientDraft): IngredientDraft {
        val quantity = when {
            a.quantity != null && b.quantity != null -> a.quantity + b.quantity
            a.quantity != null -> a.quantity
            else -> b.quantity
        }
        return a.copy(
            quantity = quantity,
            unit = a.unit ?: b.unit,
            uncertain = a.uncertain && b.uncertain,
            confidence = maxOf(a.confidence, b.confidence),
        )
    }

    private fun parseNumber(token: String): Double? {
        token.toDoubleOrNull()?.let { return it }
        token.replace(',', '.').toDoubleOrNull()?.let { return it }
        return NUMBER_WORDS[token]
    }

    private fun normalizeUnit(token: String): String? = UNITS[token]

    private companion object {
        const val LITTLE = "__little__"

        val POSSESSION_MARKERS = listOf(
            "tenho", "temos", "tem ", "sobrou", "sobraram", "comprei",
            "acabou", "acabaram", "em casa", "na geladeira", "no armario",
            "na despensa", "guardei", "guardado", "restou",
        )

        val SUGGESTION_MARKERS = listOf(
            "queria", "quero fazer", "gostaria", "daria para", "daria pra",
            "da para", "da pra", "podia", "sera que", "posso fazer",
            "receita de", "como fazer", "pensando em", "vontade de",
            "que tal", "preciso de uma ideia",
        )

        val LEADING_FILLER = setOf(
            "tenho", "temos", "tem", "e", "tambem", "mais", "alguns",
            "algumas", "so", "apenas", "aqui", "ainda", "ja",
        )

        val ARTICLE_PREFIX = setOf(
            "de", "do", "da", "dos", "das", "no", "na", "nos", "nas",
            "uns", "umas", "para", "pra",
        )

        val NUMBER_WORDS = mapOf(
            "um" to 1.0, "uma" to 1.0, "meio" to 0.5, "meia" to 0.5,
            "dois" to 2.0, "duas" to 2.0, "tres" to 3.0, "quatro" to 4.0,
            "cinco" to 5.0, "seis" to 6.0, "sete" to 7.0, "oito" to 8.0,
            "nove" to 9.0, "dez" to 10.0, "onze" to 11.0, "doze" to 12.0,
        )

        val UNITS = mapOf(
            "kg" to "kg", "quilo" to "kg", "quilos" to "kg", "kilo" to "kg", "kilos" to "kg",
            "g" to "g", "grama" to "g", "gramas" to "g",
            "l" to "L", "litro" to "L", "litros" to "L",
            "ml" to "ml", "mililitro" to "ml", "mililitros" to "ml",
            "xicara" to "xícara", "xicaras" to "xícaras",
            "colher" to "colher", "colheres" to "colheres",
            "unidade" to "un", "unidades" to "un", "un" to "un",
            "fatia" to "fatia", "fatias" to "fatias",
            "lata" to "lata", "latas" to "latas",
            "pacote" to "pacote", "pacotes" to "pacotes",
            "dente" to "dente", "dentes" to "dentes",
            "maco" to "maço", "macos" to "maços",
            "copo" to "copo", "copos" to "copos",
            "pote" to "pote", "potes" to "potes",
            "pitada" to "pitada", "pitadas" to "pitadas",
            "punhado" to "punhado", "punhados" to "punhados",
        )

        val COMPACT_QUANTITY = Regex("^(\\d+(?:[.,]\\d+)?)(kg|g|ml|l)$")
    }
}
