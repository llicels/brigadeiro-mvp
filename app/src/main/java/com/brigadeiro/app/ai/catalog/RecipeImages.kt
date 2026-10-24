package com.brigadeiro.app.ai.catalog

/**
 * Remote food photos for the offline catalog, keyed by recipe id.
 *
 * Source: TheMealDB (https://www.themealdb.com), which is free to hotlink.
 * Photos are illustrative and load over the network; when offline (or when a
 * URL fails) the UI falls back to the illustrated gradient + emoji tile, so the
 * app still works without internet.
 */
object RecipeImages {

    private val urls: Map<String, String> = mapOf(
        "arroz-cremoso-tomate" to "xxrxux1503070723",
        "macarrao-alho-oleo" to "5fu4ew1760524857",
        "omelete-queijo" to "hqaejl1695738653",
        "bolo-banana" to "6v583k1780093743",
        "sopa-legumes" to "x2fw9e1560460636",
        "frango-legumes" to "wyxwsp1486979827",
        "panqueca-carne" to "rwuyqx1511383174",
        "arroz-forno-milho" to "qt4i0n1763256454",
        "salada-tomate-cebola" to "bqx8mc1782684286",
        "vitamina-banana" to "pjbaq11784731571",
        "brigadeirao" to "tqtywx1468317395",
        "feijao-tropeiro" to "j8c1d51782772399",
        "atum-com-arroz" to "yypwwq1511304979",
        "tilapia-grelhada" to "ysxwuq1487323065",
        "moqueca-simples-peixe" to "vptqpw1511798500",
        "lentilha-curry" to "sstssx1487349585",
        "grao-de-bico-refogado" to "tvtxpq1511464705",
        "tofu-grelhado-legumes" to "1525874812",
        "abobrinha-refogada" to "wrpwuu1511786491",
        "homus" to "gpon5u1763801180",
        "arroz-integral-legumes" to "w8umt11583268117",
        "feijao-tropeiro-veg" to "xrxz7h1782592711",
        "salada-grao-bico" to "7ytdtz1784833420",
    ).mapValues { (_, file) ->
        "https://www.themealdb.com/images/media/meals/$file.jpg"
    }

    fun urlFor(recipeId: String): String? = urls[recipeId]
}
