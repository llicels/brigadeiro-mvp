package com.brigadeiro.app.ai.extractor

import com.brigadeiro.app.domain.model.IngredientCategory

/**
 * Small PT-BR food lexicon used by the deterministic extractor. It keeps the
 * MVP useful offline without shipping a model. Unknown words still extract as
 * "Outros" — the lexicon only improves canonicalisation, category and emoji.
 */
object IngredientLexicon {

    data class Entry(
        val canonical: String,
        val category: IngredientCategory,
    )

    private val entries: Map<String, Entry> = buildMap {
        fun put(canonical: String, category: IngredientCategory, vararg aliases: String) {
            val entry = Entry(canonical, category)
            put(canonical, entry)
            aliases.forEach { put(it, entry) }
        }

        // Hortifruti
        put("tomate", IngredientCategory.Produce, "tomates")
        put("cebola", IngredientCategory.Produce, "cebolas", "cebolinha")
        put("alho", IngredientCategory.Produce, "dente de alho", "dentes de alho")
        put("batata", IngredientCategory.Produce, "batatas")
        put("cenoura", IngredientCategory.Produce, "cenouras")
        put("abobrinha", IngredientCategory.Produce, "abobrinhas")
        put("berinjela", IngredientCategory.Produce)
        put("pimentao", IngredientCategory.Produce, "pimentoes", "pimentao verde", "pimentao vermelho")
        put("alface", IngredientCategory.Produce)
        put("couve", IngredientCategory.Produce)
        put("brocolis", IngredientCategory.Produce)
        put("espinafre", IngredientCategory.Produce)
        put("limao", IngredientCategory.Produce, "limoes")
        put("laranja", IngredientCategory.Produce, "laranjas")
        put("banana", IngredientCategory.Produce, "bananas")
        put("maca", IngredientCategory.Produce, "macas", "maça")
        put("morango", IngredientCategory.Produce, "morangos")
        put("abacate", IngredientCategory.Produce)
        put("milho", IngredientCategory.Produce, "milho verde")
        put("ervilha", IngredientCategory.Produce)
        put("salsinha", IngredientCategory.Produce, "cheiro verde")
        put("coentro", IngredientCategory.Produce)
        put("cebolinha verde", IngredientCategory.Produce)
        put("cogumelo", IngredientCategory.Produce, "cogumelos", "champignon")

        // Grãos
        put("arroz", IngredientCategory.Grains)
        put("feijao", IngredientCategory.Grains, "feijões", "feijao carioca", "feijao preto")
        put("macarrao", IngredientCategory.Grains, "macarrão", "massa", "espaguete", "penne")
        put("lentilha", IngredientCategory.Grains)
        put("grao de bico", IngredientCategory.Grains, "grao-de-bico")
        put("aveia", IngredientCategory.Grains)
        put("farinha", IngredientCategory.Grains, "farinha de trigo", "farinha de mandioca")
        put("pao", IngredientCategory.Grains, "pão", "paes", "pao frances")
        put("quinoa", IngredientCategory.Grains)

        // Laticínios
        put("leite", IngredientCategory.Dairy)
        put("queijo", IngredientCategory.Dairy, "queijos")
        put("queijo mussarela", IngredientCategory.Dairy, "mussarela", "muçarela", "mussarela de búfala")
        put("queijo prato", IngredientCategory.Dairy, "prato")
        put("queijo minas", IngredientCategory.Dairy)
        put("requeijao", IngredientCategory.Dairy, "requeijão")
        put("manteiga", IngredientCategory.Dairy)
        put("margarina", IngredientCategory.Dairy)
        put("creme de leite", IngredientCategory.Dairy)
        put("leite condensado", IngredientCategory.Dairy)
        put("iogurte", IngredientCategory.Dairy)
        put("parmesao", IngredientCategory.Dairy, "parmesão", "queijo parmesao")

        // Proteínas
        put("ovo", IngredientCategory.Proteins, "ovos")
        put("frango", IngredientCategory.Proteins, "peito de frango", "file de frango")
        put("carne", IngredientCategory.Proteins, "carne bovina", "bife", "patinho")
        put("carne moida", IngredientCategory.Proteins, "carne moída")
        put("porco", IngredientCategory.Proteins, "carne de porco", "lombo")
        put("peixe", IngredientCategory.Proteins, "tilapia", "tilápia", "salmão", "salmao")
        put("camarao", IngredientCategory.Proteins, "camarão")
        put("salsicha", IngredientCategory.Proteins)
        put("bacon", IngredientCategory.Proteins)
        put("atum", IngredientCategory.Proteins, "atum em lata")
        put("sardinha", IngredientCategory.Proteins)
        put("tofu", IngredientCategory.Proteins)

        // Mercearia
        put("azeite", IngredientCategory.Grocery, "azeite de oliva", "óleo", "oleo")
        put("sal", IngredientCategory.Grocery)
        put("acucar", IngredientCategory.Grocery, "açúcar")
        put("pimenta", IngredientCategory.Grocery, "pimenta do reino")
        put("oregano", IngredientCategory.Grocery, "orégano")
        put("manjericao", IngredientCategory.Grocery, "manjericão")
        put("ketchup", IngredientCategory.Grocery)
        put("mostarda", IngredientCategory.Grocery)
        put("maionese", IngredientCategory.Grocery)
        put("molho de tomate", IngredientCategory.Grocery, "extrato de tomate")
        put("caldo", IngredientCategory.Grocery, "caldo de legumes", "caldo de carne")
        put("cafe", IngredientCategory.Grocery, "café")
        put("chocolate", IngredientCategory.Grocery, "cacau")
        put("mel", IngredientCategory.Grocery)
        put("fermento", IngredientCategory.Grocery)
    }

    fun lookup(name: String): Entry? = entries[name]
}
