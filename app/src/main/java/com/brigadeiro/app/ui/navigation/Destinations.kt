package com.brigadeiro.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val HOME = "home"
    const val KITCHEN = "kitchen"
    const val IDEAS = "ideas"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val DIET = "diet"
    const val SCAN = "scan"
    const val CONFIRM = "confirm?text={text}"
    const val RECIPE = "recipe/{recipeId}"

    const val ARG_TEXT = "text"
    const val ARG_RECIPE_ID = "recipeId"

    fun confirm(text: String): String = "confirm?text=${encode(text)}"
    fun recipe(recipeId: String): String = "recipe/${encode(recipeId)}"

    private fun encode(value: String): String = android.net.Uri.encode(value)
}

data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val topLevelDestinations = listOf(
    TopLevelDestination(Routes.HOME, "Início", Icons.Outlined.Home),
    TopLevelDestination(Routes.KITCHEN, "Cozinha", Icons.Outlined.Icecream),
    TopLevelDestination(Routes.IDEAS, "Ideias", Icons.Outlined.Lightbulb),
    TopLevelDestination(Routes.PROFILE, "Perfil", Icons.Outlined.PersonOutline),
)
