package com.brigadeiro.app.domain.diet

import com.brigadeiro.app.domain.model.DietProfile
import com.brigadeiro.app.domain.model.DietType
import com.brigadeiro.app.domain.model.Restriction

/**
 * Pure policy that decides which [FoodTag]s a [DietProfile] forbids.
 *
 * Diet type is a hard rule; restrictions remove one additional tag each.
 */
object DietPolicy {

    fun forbiddenTags(profile: DietProfile): Set<FoodTag> {
        val forbidden = mutableSetOf<FoodTag>()
        when (profile.dietType) {
            DietType.Vegan -> forbidden += setOf(
                FoodTag.MEAT,
                FoodTag.FISH_SEAFOOD,
                FoodTag.DAIRY,
                FoodTag.EGG,
            )
            DietType.Vegetarian -> forbidden += setOf(FoodTag.MEAT, FoodTag.FISH_SEAFOOD)
            DietType.Pescatarian -> forbidden += FoodTag.MEAT
            DietType.Omnivore -> Unit
        }
        profile.restrictions.forEach { restriction ->
            when (restriction) {
                Restriction.Lactose -> forbidden += FoodTag.DAIRY
                Restriction.Gluten -> forbidden += FoodTag.GLUTEN
                Restriction.Eggs -> forbidden += FoodTag.EGG
                Restriction.Seafood -> forbidden += FoodTag.FISH_SEAFOOD
                Restriction.Nuts -> forbidden += FoodTag.NUTS
                Restriction.Soy -> forbidden += FoodTag.SOY
            }
        }
        return forbidden
    }

    fun isAllowed(tags: Set<FoodTag>, profile: DietProfile): Boolean =
        tags.none { it in forbiddenTags(profile) }

    fun isAllowed(name: String, profile: DietProfile): Boolean =
        isAllowed(DietClassifier.tagsOf(name), profile)

    /** Human-readable list of what the profile avoids, for prompts and UI. */
    fun forbiddenDescription(profile: DietProfile): String {
        val labels = forbiddenTags(profile).map { tag ->
            when (tag) {
                FoodTag.MEAT -> "carnes"
                FoodTag.FISH_SEAFOOD -> "peixes e frutos do mar"
                FoodTag.DAIRY -> "laticínios"
                FoodTag.EGG -> "ovos"
                FoodTag.GLUTEN -> "glúten"
                FoodTag.NUTS -> "castanhas"
                FoodTag.SOY -> "soja"
            }
        }
        return if (labels.isEmpty()) "nenhuma restrição alimentar" else labels.joinToString(", ")
    }
}
