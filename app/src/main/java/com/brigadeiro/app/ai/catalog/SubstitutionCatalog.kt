package com.brigadeiro.app.ai.catalog

import com.brigadeiro.app.domain.model.TextNormalizer

/**
 * Offline, deterministic substitution hints. They are presented as *options*
 * ("você pode trocar por..."), never as rules or judgements, and never as
 * medical/nutritional claims.
 */
object SubstitutionCatalog {

    private val byIngredient: Map<String, List<String>> = mapOf(
        "leite" to listOf("bebida vegetal", "leite sem lactose"),
        "leite condensado" to listOf("doce de leite", "creme de leite com açúcar"),
        "queijo" to listOf("queijo vegano", "requeijão", "muçarela"),
        "manteiga" to listOf("margarina", "óleo", "azeite"),
        "creme de leite" to listOf("creme vegetal", "leite de coco"),
        "azeite" to listOf("óleo", "manteiga"),
        "arroz" to listOf("arroz integral", "arroz parboilizado"),
        "macarrao" to listOf("espaguete", "penne", "macarrão integral"),
        "farinha" to listOf("farinha integral", "farinha de aveia"),
        "acucar" to listOf("açúcar mascavo", "mel", "adoçante culinário"),
        "ovo" to listOf("linhaça hidratada", "banana amassada"),
        "frango" to listOf("tofu", "grão-de-bico"),
        "carne moida" to listOf("proteína de soja", "lentilha"),
        "bacon" to listOf("cogumelo", "tofu defumado"),
        "caldo" to listOf("caldo caseiro", "água com temperos"),
        "limao" to listOf("vinagre", "laranja"),
        "shoyu" to listOf("tamari", "molho de coco"),
        "aveia" to listOf("granola", "flocos de quinoa"),
        "tahine" to listOf("pasta de amendoim", "sementes de girassol batidas"),
        "milho" to listOf("milho verde", "ervilha"),
    )

    fun alternativesFor(ingredientName: String): List<String> =
        byIngredient[TextNormalizer.normalize(ingredientName)].orEmpty()
}
