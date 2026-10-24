package com.brigadeiro.app.ai.receipt

import com.brigadeiro.app.ai.extractor.IngredientLexicon
import com.brigadeiro.app.domain.model.IngredientCategory
import com.brigadeiro.app.domain.model.IngredientDraft
import com.brigadeiro.app.domain.model.TextNormalizer

/**
 * Deterministic parser that turns OCR lines from a grocery receipt into
 * ingredient drafts. It is intentionally conservative: header/footer/total
 * lines are dropped, items are matched against the food lexicon, and anything
 * not recognised is kept but flagged [IngredientDraft.uncertain] so the user
 * reviews it. Nothing is ever saved without confirmation.
 */
object ReceiptParser {

    private val noiseWords = listOf(
        "cnpj", "cpf", "cupom", "fiscal", "total", "subtotal", "troco",
        "dinheiro", "cartao", "cartão", "pix", "desconto", "valor", "item",
        "obrigad", "sat ", "nfce", "nf-e", "chave", "protocolo", "operador",
        "caixa", "loja", "qtd", "crédito", "credito", "débito", "debito",
        "banco", "autoriz", "consumidor", "tributos", "imposto", "endereco",
        "endereço", "rua ", "av.", "avenida", "fone", "telefone",
    )

    private val priceRegex = Regex("""(?:R\$\s*)?\d{1,4}[.,]\d{2}\s*$""")
    private val leadingCode = Regex("""^\s*\d{2,8}\s+""")
    private val quantityUnit = Regex(
        """(\d+(?:[.,]\d+)?)\s*(kg|g|ml|l|un|und|unid|unidade|cx|pct|dz)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val nonLetters = Regex("""[^A-Za-zÀ-ÿ\s]""")
    private val spaces = Regex("""\s+""")

    fun parse(text: String): List<IngredientDraft> =
        parse(text.split('\n'))

    fun parse(lines: List<String>): List<IngredientDraft> {
        val result = LinkedHashMap<String, IngredientDraft>()

        lines.forEach { raw ->
            val line = raw.trim()
            if (line.length < 3) return@forEach

            val lower = line.lowercase()
            if (noiseWords.any { lower.contains(it) }) return@forEach

            val hasPrice = priceRegex.containsMatchIn(line)
            val withoutPrice = priceRegex.replace(line, "").trim()
            val cleaned = leadingCode.replace(withoutPrice, "").trim()

            val namePart = cleaned
                .replace(quantityUnit, " ")
                .replace(nonLetters, " ")
                .replace(spaces, " ")
                .trim()
            if (namePart.length < 3) return@forEach

            val normalized = TextNormalizer.normalize(namePart)
            val entry = lexiconMatch(normalized)

            // Keep lexicon hits even without a price; only keep price-bearing
            // lines otherwise (avoids random receipt text).
            val draft = when {
                entry != null -> IngredientDraft(
                    name = entry.canonical,
                    quantity = quantityOf(cleaned),
                    unit = unitOf(cleaned),
                    category = entry.category,
                    uncertain = false,
                    confidence = 0.9,
                )
                hasPrice -> IngredientDraft(
                    name = titleCase(namePart),
                    quantity = quantityOf(cleaned),
                    unit = unitOf(cleaned),
                    category = IngredientCategory.Others,
                    uncertain = true,
                    confidence = 0.4,
                )
                else -> return@forEach
            }

            val key = TextNormalizer.normalize(draft.name)
            if (key.isNotEmpty() && !result.containsKey(key)) result[key] = draft
        }

        return result.values.toList()
    }

    private fun lexiconMatch(normalized: String): IngredientLexicon.Entry? {
        val words = normalized.split(' ').filter { it.length >= 3 }
        words.forEach { word -> IngredientLexicon.lookup(word)?.let { return it } }
        for (i in 0 until words.size - 1) {
            IngredientLexicon.lookup("${words[i]} ${words[i + 1]}")?.let { return it }
        }
        return null
    }

    private fun quantityOf(value: String): Double? =
        quantityUnit.find(value)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()

    private fun unitOf(value: String): String? =
        quantityUnit.find(value)?.groupValues?.get(2)?.lowercase()

    private fun titleCase(value: String): String = value
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.lowercase().replaceFirstChar { it.titlecase() }
        }
}
