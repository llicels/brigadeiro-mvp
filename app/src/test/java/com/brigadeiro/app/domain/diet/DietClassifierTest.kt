package com.brigadeiro.app.domain.diet

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DietClassifierTest {

    @Test
    fun tagsMeatFishDairy() {
        assertThat(DietClassifier.tagsOf("frango")).contains(FoodTag.MEAT)
        assertThat(DietClassifier.tagsOf("salmão")).contains(FoodTag.FISH_SEAFOOD)
        assertThat(DietClassifier.tagsOf("queijo mussarela")).contains(FoodTag.DAIRY)
        assertThat(DietClassifier.tagsOf("ovo")).contains(FoodTag.EGG)
    }

    @Test
    fun tagsPlantIngredientsByTheirOwnCategory() {
        assertThat(DietClassifier.tagsOf("tomate")).isEmpty()
        assertThat(DietClassifier.tagsOf("tofu")).containsExactly(FoodTag.SOY)
        assertThat(DietClassifier.tagsOf("pasta de amendoim")).containsExactly(FoodTag.NUTS)
        assertThat(DietClassifier.tagsOf("leite de amêndoa")).containsExactly(FoodTag.NUTS)
    }

    @Test
    fun aggregatesTagsAcrossIngredients() {
        val tags = DietClassifier.tagsOf(listOf("farinha", "leite", "ovo"))

        assertThat(tags).containsExactly(FoodTag.GLUTEN, FoodTag.DAIRY, FoodTag.EGG)
    }
}
