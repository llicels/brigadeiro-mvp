package com.brigadeiro.app.ai.catalog

import com.brigadeiro.app.ai.RecipeGenerator
import com.brigadeiro.app.domain.diet.DietClassifier
import com.brigadeiro.app.domain.diet.DietPolicy
import com.brigadeiro.app.domain.model.DietProfile
import com.brigadeiro.app.domain.model.Equipment
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.IngredientSubstitution
import com.brigadeiro.app.domain.model.Recipe
import com.brigadeiro.app.domain.model.RecipeIngredient
import com.brigadeiro.app.domain.model.RecipeStep
import com.brigadeiro.app.domain.model.RecipeSuggestion
import com.brigadeiro.app.domain.model.TextNormalizer

/**
 * Offline, deterministic recipe generator. Discards recipes that conflict with
 * the user's diet or require equipment they don't have, scores the rest against
 * the current inventory, applies an optional time cap and, when the user asked
 * for freezer-friendly meals, floats those to the top.
 */
class CatalogRecipeGenerator(
    private val catalog: List<CatalogRecipe> = RecipeCatalog.recipes,
) : RecipeGenerator {

    override suspend fun suggestRecipes(
        ingredients: List<Ingredient>,
        profile: DietProfile,
        maxTotalMinutes: Int?,
    ): List<RecipeSuggestion> {
        if (ingredients.isEmpty()) return emptyList()
        val inventory = ingredients.map { TextNormalizer.normalize(it.name) }.toSet()

        return catalog
            .filter { recipe -> recipe.requiredEquipment.all { it in profile.equipment } }
            .filter { recipe -> maxTotalMinutes == null || recipe.timeMinutes <= maxTotalMinutes }
            .filter { recipe ->
                DietPolicy.isAllowed(
                    DietClassifier.tagsOf(recipe.ingredients.map { it.name }),
                    profile,
                )
            }
            .map { it to match(it, inventory) }
            .filter { (_, match) -> match.available.isNotEmpty() }
            .sortedWith(comparator(profile.preferFreezerFriendly))
            .map { (recipe, match) ->
                RecipeSuggestion(
                    id = recipe.id,
                    title = recipe.title,
                    timeMinutes = recipe.timeMinutes,
                    activeTimeMinutes = recipe.activeTimeMinutes,
                    servings = recipe.servings,
                    difficulty = recipe.difficulty,
                    estimatedCostBRL = recipe.costPerServing,
                    imageSlug = recipe.id,
                    imageEmoji = recipe.imageEmoji,
                    imageUrl = RecipeImages.urlFor(recipe.id),
                    availableIngredients = match.available,
                    missingIngredients = match.missing,
                    freezerFriendly = recipe.freezerFriendly,
                )
            }
    }

    override suspend fun generateRecipe(
        suggestion: RecipeSuggestion,
        ingredients: List<Ingredient>,
        profile: DietProfile,
    ): Recipe {
        val recipe = catalog.firstOrNull { it.id == suggestion.id }
            ?: catalog.firstOrNull { it.title == suggestion.title }
            ?: error("Receita não encontrada: ${suggestion.id}")
        val inventory = ingredients.map { TextNormalizer.normalize(it.name) }.toSet()

        return Recipe(
            id = recipe.id,
            title = recipe.title,
            timeMinutes = recipe.timeMinutes,
            activeTimeMinutes = recipe.activeTimeMinutes,
            servings = recipe.servings,
            difficulty = recipe.difficulty,
            estimatedCostBRL = recipe.costPerServing,
            imageSlug = recipe.id,
            imageEmoji = recipe.imageEmoji,
            imageUrl = RecipeImages.urlFor(recipe.id),
            ingredients = recipe.ingredients.map { ingredient ->
                RecipeIngredient(
                    name = ingredient.name,
                    quantity = ingredient.quantity,
                    available = isAvailable(ingredient.name, inventory),
                )
            },
            steps = recipe.steps.mapIndexed { index, text -> RecipeStep(index + 1, text) },
            substitutions = recipe.ingredients
                .mapNotNull { ingredient ->
                    val alternatives = SubstitutionCatalog.alternativesFor(ingredient.name)
                    if (alternatives.isEmpty()) {
                        null
                    } else {
                        IngredientSubstitution(ingredient.name, alternatives)
                    }
                },
            requiredEquipment = recipe.requiredEquipment
                .filter { it in profile.equipment }
                .ifEmpty { recipe.requiredEquipment.toList() }
                .sortedBy { it.ordinal },
            freezerFriendly = recipe.freezerFriendly,
        )
    }

    private fun comparator(preferFreezer: Boolean): Comparator<Pair<CatalogRecipe, Match>> {
        val byAvailability = compareByDescending<Pair<CatalogRecipe, Match>> {
            it.second.available.size
        }
        val byMissing = compareBy<Pair<CatalogRecipe, Match>> { it.second.missing.size }
        val byFreezer = compareByDescending<Pair<CatalogRecipe, Match>> {
            it.first.freezerFriendly
        }
        val byTime = compareBy<Pair<CatalogRecipe, Match>> { it.first.timeMinutes }

        return if (preferFreezer) {
            byFreezer.then(byAvailability).then(byMissing).then(byTime)
        } else {
            byAvailability.then(byMissing).then(byFreezer).then(byTime)
        }
    }

    private fun match(recipe: CatalogRecipe, inventory: Set<String>): Match {
        val available = mutableListOf<String>()
        val missing = mutableListOf<String>()
        recipe.ingredients.forEach { ingredient ->
            if (isAvailable(ingredient.name, inventory)) {
                available += ingredient.name
            } else {
                missing += ingredient.name
            }
        }
        return Match(available, missing)
    }

    private fun isAvailable(name: String, inventory: Set<String>): Boolean {
        val normalized = TextNormalizer.normalize(name)
        return inventory.any { owned ->
            owned == normalized || owned.contains(normalized) || normalized.contains(owned)
        }
    }

    private data class Match(val available: List<String>, val missing: List<String>)
}
