package com.brigadeiro.app.domain.diet

import com.brigadeiro.app.domain.model.DietProfile
import com.brigadeiro.app.domain.model.DietType
import com.brigadeiro.app.domain.model.Restriction
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DietPolicyTest {

    private fun profile(
        diet: DietType = DietType.Omnivore,
        restrictions: Set<Restriction> = emptySet(),
    ) = DietProfile(dietType = diet, restrictions = restrictions, onboarded = true)

    @Test
    fun veganForbidsAllAnimalProducts() {
        val p = profile(DietType.Vegan)
        assertThat(DietPolicy.isAllowed("frango", p)).isFalse()
        assertThat(DietPolicy.isAllowed("tilápia", p)).isFalse()
        assertThat(DietPolicy.isAllowed("queijo", p)).isFalse()
        assertThat(DietPolicy.isAllowed("ovo", p)).isFalse()
        assertThat(DietPolicy.isAllowed("arroz", p)).isTrue()
        assertThat(DietPolicy.isAllowed("tofu", p)).isTrue()
    }

    @Test
    fun pescatarianAllowsFishEggsAndDairy() {
        val p = profile(DietType.Pescatarian)
        assertThat(DietPolicy.isAllowed("carne", p)).isFalse()
        assertThat(DietPolicy.isAllowed("bacon", p)).isFalse()
        assertThat(DietPolicy.isAllowed("salmão", p)).isTrue()
        assertThat(DietPolicy.isAllowed("ovo", p)).isTrue()
        assertThat(DietPolicy.isAllowed("queijo mussarela", p)).isTrue()
    }

    @Test
    fun vegetarianAllowsEggsAndDairyButNotFish() {
        val p = profile(DietType.Vegetarian)
        assertThat(DietPolicy.isAllowed("camarão", p)).isFalse()
        assertThat(DietPolicy.isAllowed("ovo", p)).isTrue()
        assertThat(DietPolicy.isAllowed("iogurte", p)).isTrue()
    }

    @Test
    fun lactoseRestrictionForbidsDairy() {
        val p = profile(DietType.Omnivore, setOf(Restriction.Lactose))
        assertThat(DietPolicy.isAllowed("leite", p)).isFalse()
        assertThat(DietPolicy.isAllowed("queijo", p)).isFalse()
        assertThat(DietPolicy.isAllowed("leite de coco", p)).isTrue()
    }

    @Test
    fun glutenRestrictionForbidsWheatProducts() {
        val p = profile(DietType.Omnivore, setOf(Restriction.Gluten))
        assertThat(DietPolicy.isAllowed("farinha de trigo", p)).isFalse()
        assertThat(DietPolicy.isAllowed("macarrão", p)).isFalse()
        assertThat(DietPolicy.isAllowed("arroz", p)).isTrue()
    }

    @Test
    fun avoidsFalseDairyOnPlantMilkAndPeanutButter() {
        val p = profile(DietType.Omnivore, setOf(Restriction.Lactose, Restriction.Nuts))
        assertThat(DietPolicy.isAllowed("leite de amêndoa", p)).isFalse() // nuts, not dairy
        assertThat(DietPolicy.isAllowed("pasta de amendoim", p)).isFalse() // nuts, not dairy
    }
}
