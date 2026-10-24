package com.brigadeiro.app.domain.model

/**
 * Kitchen equipment the user can tell us about. Recipes that require an
 * unavailable item are hidden from the main recommendations (see the catalog
 * generator). The default set is "everything", so a user who skips the question
 * sees the full catalog.
 */
enum class Equipment(val label: String, val emoji: String) {
    Stove("Fogão", "\uD83D\uDD25"),
    Oven("Forno", "\u2668\uFE0F"),
    Microwave("Micro-ondas", "\uD83D\uDCF6"),
    AirFryer("Air fryer", "\uD83C\uDF5F"),
    Blender("Liquidificador", "\uD83E\uDD64"),
    SandwichMaker("Sanduicheira", "\uD83E\uDD6A"),
    PressureCooker("Panela de pressão", "\uD83D\uDED0"),
    Mixer("Mixer", "\uD83C\uDF00"),
    ;

    companion object {
        /** Equipment pre-selected when the user has not customised anything. */
        val defaults: Set<Equipment> = setOf(
            Stove,
            Oven,
            Microwave,
            Blender,
        )

        val all: Set<Equipment> = entries.toSet()
    }
}
