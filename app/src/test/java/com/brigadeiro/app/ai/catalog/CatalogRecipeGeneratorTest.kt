package com.brigadeiro.app.ai.catalog

import com.brigadeiro.app.domain.model.DietProfile
import com.brigadeiro.app.domain.model.DietType
import com.brigadeiro.app.domain.model.Equipment
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.IngredientCategory
import com.brigadeiro.app.domain.model.Restriction
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CatalogRecipeGeneratorTest {

    private val generator = CatalogRecipeGenerator()
    private val omnivore = DietProfile(onboarded = true)

    private fun ingredient(name: String) = Ingredient(
        name = name,
        normalizedName = com.brigadeiro.app.domain.model.TextNormalizer.normalize(name),
        category = IngredientCategory.Others,
    )

    @Test
    fun suggestsRecipesUsingInventory() = runTest {
        val suggestions = generator.suggestRecipes(
            listOf(ingredient("arroz"), ingredient("tomate"), ingredient("queijo")),
            omnivore,
        )

        assertThat(suggestions).isNotEmpty()
        val arrozCremoso = suggestions.firstOrNull { it.id == "arroz-cremoso-tomate" }
        assertThat(arrozCremoso).isNotNull()
        assertThat(arrozCremoso!!.availableIngredients).contains("arroz")
        assertThat(arrozCremoso.missingIngredients).contains("cebola")
    }

    @Test
    fun emptyInventoryYieldsNoSuggestions() = runTest {
        assertThat(generator.suggestRecipes(emptyList(), omnivore)).isEmpty()
    }

    @Test
    fun vegetarianProfileExcludesMeatAndFishRecipes() = runTest {
        val ingredients = listOf(
            ingredient("carne moida"), ingredient("frango"), ingredient("peixe"),
            ingredient("farinha"), ingredient("arroz"), ingredient("tomate"),
        )
        val profile = DietProfile(dietType = DietType.Vegetarian, onboarded = true)

        val suggestions = generator.suggestRecipes(ingredients, profile)

        assertThat(suggestions.map { it.id })
            .doesNotContain("feijao-tropeiro")
        assertThat(suggestions.map { it.id })
            .doesNotContain("panqueca-carne")
        assertThat(suggestions.map { it.id })
            .doesNotContain("frango-legumes")
    }

    @Test
    fun veganProfileExcludesDairyRecipes() = runTest {
        val ingredients = listOf(ingredient("arroz"), ingredient("queijo"), ingredient("milho"))
        val profile = DietProfile(dietType = DietType.Vegan, onboarded = true)

        val suggestions = generator.suggestRecipes(ingredients, profile)

        assertThat(suggestions.map { it.id }).doesNotContain("arroz-forno-milho")
    }

    @Test
    fun lactoseRestrictionExcludesDairy() = runTest {
        val ingredients = listOf(ingredient("banana"), ingredient("leite"), ingredient("aveia"))
        val profile = DietProfile(restrictions = setOf(Restriction.Lactose), onboarded = true)

        val suggestions = generator.suggestRecipes(ingredients, profile)

        assertThat(suggestions.map { it.id }).doesNotContain("vitamina-banana")
    }

    @Test
    fun freezerPreferenceFloatsFreezerRecipesFirst() = runTest {
        val ingredients = listOf(ingredient("arroz"), ingredient("tomate"), ingredient("cebola"))
        val profile = DietProfile(preferFreezerFriendly = true, onboarded = true)

        val suggestions = generator.suggestRecipes(ingredients, profile)

        assertThat(suggestions.first().freezerFriendly).isTrue()
    }

    @Test
    fun generatesFullRecipeWithAvailability() = runTest {
        val ingredients = listOf(ingredient("ovo"), ingredient("queijo"))
        val suggestion = generator.suggestRecipes(ingredients, omnivore)
            .first { it.id == "omelete-queijo" }

        val recipe = generator.generateRecipe(suggestion, ingredients, omnivore)

        assertThat(recipe.title).isEqualTo("Omelete de queijo")
        assertThat(recipe.steps).isNotEmpty()
        assertThat(recipe.ingredients.first { it.name == "ovo" }.available).isTrue()
        assertThat(recipe.ingredients.first { it.name == "sal" }.available).isFalse()
    }

    @Test
    fun suggestionCarriesTimeCostAndDifficulty() = runTest {
        val ingredients = listOf(ingredient("arroz"), ingredient("tomate"), ingredient("queijo"))
        val suggestion = generator.suggestRecipes(ingredients, omnivore)
            .first { it.id == "arroz-cremoso-tomate" }

        assertThat(suggestion.timeMinutes).isEqualTo(25)
        assertThat(suggestion.activeTimeMinutes).isEqualTo(15)
        assertThat(suggestion.estimatedCostBRL).isEqualTo(4.5)
        assertThat(suggestion.difficulty).isEqualTo("fácil")
    }

    @Test
    fun timeFilterOnlyKeepsQuickRecipes() = runTest {
        val ingredients = listOf(ingredient("arroz"), ingredient("tomate"), ingredient("cebola"))

        val suggestions = generator.suggestRecipes(ingredients, omnivore, maxTotalMinutes = 20)

        assertThat(suggestions).isNotEmpty()
        assertThat(suggestions.all { it.timeMinutes <= 20 }).isTrue()
    }

    @Test
    fun hidesRecipesThatNeedMissingEquipment() = runTest {
        val ingredients = listOf(
            ingredient("banana"), ingredient("ovo"), ingredient("farinha"),
            ingredient("acucar"), ingredient("manteiga"), ingredient("fermento"),
        )
        val stoveOnly = DietProfile(equipment = setOf(Equipment.Stove), onboarded = true)

        val suggestions = generator.suggestRecipes(ingredients, stoveOnly)

        assertThat(suggestions.map { it.id }).doesNotContain("bolo-banana")
    }

    @Test
    fun generatedRecipeIncludesSubstitutionOptions() = runTest {
        val ingredients = listOf(ingredient("ovo"), ingredient("queijo"))
        val suggestion = generator.suggestRecipes(ingredients, omnivore)
            .first { it.id == "omelete-queijo" }

        val recipe = generator.generateRecipe(suggestion, ingredients, omnivore)

        val cheese = recipe.substitutions.firstOrNull { it.ingredient == "queijo" }
        assertThat(cheese).isNotNull()
        assertThat(cheese!!.alternatives).isNotEmpty()
    }
}
