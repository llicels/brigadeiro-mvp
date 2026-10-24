package com.brigadeiro.app.domain.model

/**
 * A candidate ingredient produced by the extraction pipeline. Drafts are shown on
 * the confirmation screen and only persisted after the user confirms them.
 *
 * [uncertain] marks items the extractor is not sure about (e.g. an ambiguous
 * sentence) so the UI can ask for confirmation instead of assuming.
 */
data class IngredientDraft(
    val name: String,
    val quantity: Double? = null,
    val unit: String? = null,
    val category: IngredientCategory = IngredientCategory.Others,
    val uncertain: Boolean = false,
    val confidence: Double = 1.0,
) {
    val displayQuantity: String
        get() = formatQuantity(quantity, unit)
}

/**
 * Structural result of the extraction step, independent of any AI provider.
 */
data class ExtractionResult(
    val drafts: List<IngredientDraft>,
    val sourceText: String = "",
) {
    val isEmpty: Boolean get() = drafts.isEmpty()
}

/** Formats a quantity/unit pair for display, e.g. 2 -> "2 un", 0.5 kg -> "0.5 kg". */
fun formatQuantity(quantity: Double?, unit: String?): String {
    if (quantity == null) return ""
    val number = if (quantity % 1.0 == 0.0) quantity.toLong().toString() else trimNumber(quantity)
    return if (unit.isNullOrBlank()) number else "$number $unit"
}

private fun trimNumber(value: Double): String {
    val rounded = kotlin.math.round(value * 100) / 100
    return rounded.toString().trimEnd('0').trimEnd('.')
}
