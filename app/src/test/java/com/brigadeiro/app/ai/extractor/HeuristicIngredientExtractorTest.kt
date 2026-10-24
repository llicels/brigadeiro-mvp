package com.brigadeiro.app.ai.extractor

import com.brigadeiro.app.domain.model.IngredientCategory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HeuristicIngredientExtractorTest {

    private val extractor = HeuristicIngredientExtractor()

    @Test
    fun extractsQuantitiesAndNamesFromPossession() = runTest {
        val result = extractor.extract("Tenho dois tomates, arroz e queijo mussarela")

        assertThat(result.drafts).hasSize(3)
        val tomato = result.drafts.first { it.name == "tomate" }
        assertThat(tomato.quantity).isEqualTo(2.0)
        assertThat(tomato.category).isEqualTo(IngredientCategory.Produce)

        val rice = result.drafts.first { it.name == "arroz" }
        assertThat(rice.quantity).isNull()

        val cheese = result.drafts.first { it.name == "queijo mussarela" }
        assertThat(cheese.category).isEqualTo(IngredientCategory.Dairy)
    }

    @Test
    fun ignoresPureSuggestions() = runTest {
        val result = extractor.extract("Acho que daria para fazer macarrão com tomate")

        assertThat(result.drafts).isEmpty()
    }

    @Test
    fun treatsBareListAsInventory() = runTest {
        val result = extractor.extract("arroz, tomate, queijo")

        assertThat(result.drafts.map { it.name }).containsExactly("arroz", "tomate", "queijo")
    }

    @Test
    fun keepsQuantityAndUnit() = runTest {
        val result = extractor.extract("Tenho meio quilo de carne e 500g de farinha")

        val meat = result.drafts.first { it.name == "carne" }
        assertThat(meat.quantity).isEqualTo(0.5)
        assertThat(meat.unit).isEqualTo("kg")

        val flour = result.drafts.first { it.name == "farinha" }
        assertThat(flour.quantity).isEqualTo(500.0)
        assertThat(flour.unit).isEqualTo("g")
    }

    @Test
    fun mergesDuplicatedIngredients() = runTest {
        val result = extractor.extract("Tenho 2 tomates e mais 3 tomates")

        assertThat(result.drafts).hasSize(1)
        assertThat(result.drafts.first().quantity).isEqualTo(5.0)
    }

    @Test
    fun blankInputReturnsEmpty() = runTest {
        assertThat(extractor.extract("   ").drafts).isEmpty()
    }
}
