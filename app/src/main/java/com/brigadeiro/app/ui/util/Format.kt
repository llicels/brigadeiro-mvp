package com.brigadeiro.app.ui.util

import java.time.Instant
import java.time.ZoneId
import java.util.Locale

fun greeting(epochMs: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
    val hour = Instant.ofEpochMilli(epochMs).atZone(zoneId).hour
    return when {
        hour < 12 -> "Olá!"
        hour < 18 -> "Boa tarde!"
        else -> "Boa noite!"
    }
}

/** "R$ 4,50" */
fun currencyBRL(value: Double): String =
    "R$ " + String.format(Locale("pt", "BR"), "%.2f", value).replace('.', ',')

/** "~R$ 4,50 / porção" — always an estimate in the MVP. */
fun costLabel(value: Double): String = "~${currencyBRL(value)} / porção"

/** Compact estimate for tight card layouts: "~R$ 4,50". */
fun shortCostLabel(value: Double): String = "~${currencyBRL(value)}"

/**
 * "25 min · 15 min de preparo ativo" when there is passive time, otherwise just
 * "10 min".
 */
fun timeLabel(totalMinutes: Int, activeMinutes: Int): String =
    if (activeMinutes in 1 until totalMinutes) {
        "$totalMinutes min · $activeMinutes min de preparo ativo"
    } else {
        "$totalMinutes min"
    }

/** Traffic-light emoji for the difficulty badge. */
fun difficultyEmoji(difficulty: String): String = when (difficulty.trim().lowercase()) {
    "média", "media" -> "\uD83D\uDFE1"
    "difícil", "dificil" -> "\uD83D\uDD34"
    else -> "\uD83D\uDFE2"
}
