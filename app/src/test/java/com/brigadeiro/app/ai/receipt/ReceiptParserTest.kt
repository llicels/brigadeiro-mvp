package com.brigadeiro.app.ai.receipt

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReceiptParserTest {

    @Test
    fun extractsFoodItemsAndDropsHeaderFooter() {
        val lines = listOf(
            "SUPERMERCADO BOM PRECO",
            "CNPJ 12.345.678/0001-99",
            "CUPOM FISCAL",
            "001 ARROZ TIO JOAO 5KG 2 X 25,90 51,80",
            "005 TOMATE ITALIANO 1KG 8,90",
            "012 LEITE INTEGRAL 1L 4,99",
            "TOTAL 65,69",
            "CARTAO DEBITO 65,69",
            "OBRIGADO E VOLTE SEMPRE",
        )

        val drafts = ReceiptParser.parse(lines)
        val names = drafts.map { it.name.lowercase() }

        assertThat(names).contains("arroz")
        assertThat(names).contains("tomate")
        assertThat(names).contains("leite")
        assertThat(names).doesNotContain("supermercado bom preco")
        assertThat(names.none { it.contains("total") }).isTrue()
    }

    @Test
    fun marksUnknownPricedItemsAsUncertain() {
        val lines = listOf("999 PRODUTO DESCONHECIDO XPTO 3,50")

        val drafts = ReceiptParser.parse(lines)

        assertThat(drafts).hasSize(1)
        assertThat(drafts.first().uncertain).isTrue()
    }

    @Test
    fun lexiconOnlyLineWithoutPriceIsKept() {
        val lines = listOf("FEIJAO CARIOCA")

        val drafts = ReceiptParser.parse(lines)

        assertThat(drafts.map { it.name }).contains("feijao")
    }

    @Test
    fun emptyTextYieldsNothing() {
        assertThat(ReceiptParser.parse("")).isEmpty()
    }
}
