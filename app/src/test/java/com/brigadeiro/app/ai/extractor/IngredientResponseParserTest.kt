package com.brigadeiro.app.ai.extractor

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IngredientResponseParserTest {

    private val parser = IngredientResponseParser()

    @Test
    fun parsesValidJson() {
        val raw = """{"ingredients":[{"name":"Tomate","quantity":2,"unit":"unidade"}]}"""

        val result = parser.parse(raw)

        assertThat(result.drafts).hasSize(1)
        assertThat(result.drafts.first().name).isEqualTo("tomate")
        assertThat(result.drafts.first().quantity).isEqualTo(2.0)
    }

    @Test
    fun extractsJsonFromNoisyText() {
        val raw = "Claro! Aqui está:\n{\"ingredients\":[{\"name\":\"Arroz\"}]}\nBom apetite."

        val result = parser.parse(raw)

        assertThat(result.drafts.map { it.name }).containsExactly("arroz")
    }

    @Test
    fun dropsInvalidEntries() {
        val raw = """{"ingredients":[{"name":""},{"name":"A"},{"quantity":3}]}"""

        assertThat(parser.parse(raw).drafts).isEmpty()
    }

    @Test
    fun rejectsNegativeQuantity() {
        val raw = """{"ingredients":[{"name":"tomate","quantity":-2}]}"""

        val draft = parser.parse(raw).drafts.first()
        assertThat(draft.quantity).isNull()
    }

    @Test
    fun returnsEmptyForInvalidJson() {
        assertThat(parser.parse("not json at all").drafts).isEmpty()
        assertThat(parser.parse("{ broken").drafts).isEmpty()
    }
}
