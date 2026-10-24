package com.brigadeiro.app.domain.diet

import com.brigadeiro.app.domain.model.TextNormalizer

/**
 * Maps ingredient names to [FoodTag]s using PT-BR keywords. Deterministic and
 * offline; used for the bundled catalog and for filtering remote suggestions.
 *
 * Plant milks and peanut butter are special-cased so they are not mistaken for
 * dairy.
 */
object DietClassifier {

    fun tagsOf(name: String): Set<FoodTag> {
        val n = TextNormalizer.normalize(name)
        if (n.isEmpty()) return emptySet()
        val tags = mutableSetOf<FoodTag>()

        val plantMilk = PLANT_MILKS.any { n.contains(it) }
        val peanutButter = PEANUT_BUTTER.any { n.contains(it) }

        if (MEAT_KEYS.any { n.contains(it) }) tags += FoodTag.MEAT
        if (FISH_KEYS.any { n.contains(it) }) tags += FoodTag.FISH_SEAFOOD
        if (!plantMilk && !peanutButter && DAIRY_KEYS.any { n.contains(it) }) tags += FoodTag.DAIRY
        if (EGG_KEYS.any { n.contains(it) }) tags += FoodTag.EGG
        if (GLUTEN_KEYS.any { n.contains(it) }) tags += FoodTag.GLUTEN
        if (peanutButter || NUTS_KEYS.any { n.contains(it) }) tags += FoodTag.NUTS
        if (SOY_KEYS.any { n.contains(it) }) tags += FoodTag.SOY

        return tags
    }

    fun tagsOf(names: Iterable<String>): Set<FoodTag> =
        names.flatMap { tagsOf(it) }.toSet()

    private val PLANT_MILKS = listOf(
        "leite de coco", "leite de amendoa", "leite de aveia", "leite de arroz",
        "leite de castanha", "leite vegetal", "leite de soja", "bebida vegetal",
    )
    private val PEANUT_BUTTER = listOf("manteiga de amendoim", "pasta de amendoim")

    private val MEAT_KEYS = listOf(
        "carne", "frango", "porco", "bacon", "salsicha", "presunto", "linguica",
        "bife", "patinho", "picanha", "lombo", "costela", "contrafile", "peito de frango",
    )
    private val FISH_KEYS = listOf(
        "peixe", "tilapia", "salmao", "atum", "sardinha", "camarao", "merluza",
        "bacalhau", "polvo", "lula", "mexilhao", "ostra", "marisco",
    )
    private val DAIRY_KEYS = listOf(
        "leite", "queijo", "manteiga", "creme de leite", "requeijao", "iogurte",
        "parmesao", "mussarela", "muçarela", "ricota", "nata", "coalhada",
    )
    private val EGG_KEYS = listOf("ovo", "ovos", "gemada")
    private val GLUTEN_KEYS = listOf(
        "farinha", "pao", "macarrao", "espaguete", "penne", "trigo", "biscoito",
        "bolacha", "cerveja", "centeio", "cevada", "semolina",
    )
    private val NUTS_KEYS = listOf(
        "castanha", "amendoim", "noz", "nozes", "amendoa", "avela", "pistache",
    )
    private val SOY_KEYS = listOf("soja", "tofu", "shoyu", "edamame", "proteina de soja")
}
