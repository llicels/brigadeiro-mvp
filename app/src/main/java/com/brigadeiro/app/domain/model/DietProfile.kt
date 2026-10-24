package com.brigadeiro.app.domain.model

/** Dietary pattern. */
enum class DietType(val label: String, val emoji: String) {
    Omnivore("Como de tudo", "\uD83C\uDF5D"),
    Pescatarian("Pescatariano", "\uD83D\uDC1F"),
    Vegetarian("Vegetariano", "\uD83E\uDD66"),
    Vegan("Vegano", "\uD83C\uDF31"),
}

/** Things the user avoids for health, allergy or ethical reasons. */
enum class Restriction(val label: String) {
    Lactose("Lactose"),
    Gluten("Glúten"),
    Eggs("Ovos"),
    Seafood("Frutos do mar"),
    Nuts("Castanhas"),
    Soy("Soja"),
}

/**
 * The user's dietary profile. [onboarded] distinguishes "answered the onboarding"
 * (even if skipped) from "never asked". [equipment] carries the kitchen profile
 * (which appliances are available); it defaults to the common set so nothing is
 * hidden until the user edits it.
 */
data class DietProfile(
    val dietType: DietType = DietType.Omnivore,
    val restrictions: Set<Restriction> = emptySet(),
    val preferFreezerFriendly: Boolean = false,
    val equipment: Set<Equipment> = Equipment.defaults,
    val onboarded: Boolean = false,
) {
    /** Short human summary, e.g. "Pescatariano · sem lactose". */
    val summary: String
        get() {
            val parts = mutableListOf(dietType.label)
            restrictions
                .sortedBy { it.ordinal }
                .forEach { parts += "sem ${it.label.lowercase()}" }
            if (preferFreezerFriendly) parts += "para congelar"
            return parts.joinToString(" · ")
        }

    /** Short human summary of the kitchen, e.g. "Fogão · Forno · Micro-ondas". */
    val equipmentSummary: String
        get() = Equipment.entries
            .filter { it in equipment }
            .joinToString(" · ") { it.label }
}
