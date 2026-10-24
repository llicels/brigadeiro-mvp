package com.brigadeiro.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.brigadeiro.app.ui.screens.confirm.ConfirmScreen
import com.brigadeiro.app.ui.screens.home.HomeScreen
import com.brigadeiro.app.ui.screens.ideas.IdeasScreen
import com.brigadeiro.app.ui.screens.kitchen.KitchenScreen
import com.brigadeiro.app.ui.screens.onboarding.OnboardingScreen
import com.brigadeiro.app.ui.screens.recipe.RecipeDetailScreen
import com.brigadeiro.app.ui.screens.scan.ScanScreen
import com.brigadeiro.app.ui.screens.settings.SettingsScreen

@Composable
fun BrigadeiroNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val openTopLevel: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            TopLevelScaffold(
                selectedRoute = Routes.HOME,
                onNavigate = openTopLevel,
            ) { padding ->
                HomeScreen(
                    onOpenConfirm = { text -> navController.navigate(Routes.confirm(text)) },
                    onOpenRecipe = { id -> navController.navigate(Routes.recipe(id)) },
                    onOpenKitchen = { openTopLevel(Routes.KITCHEN) },
                    onOpenIdeas = { openTopLevel(Routes.IDEAS) },
                    onOpenSettings = { openTopLevel(Routes.PROFILE) },
                    onOpenDiet = { navController.navigate(Routes.DIET) },
                    onOpenScan = { navController.navigate(Routes.SCAN) },
                    contentPadding = padding,
                )
            }
        }

        composable(Routes.KITCHEN) {
            TopLevelScaffold(
                selectedRoute = Routes.KITCHEN,
                onNavigate = openTopLevel,
            ) { padding ->
                KitchenScreen(
                    contentPadding = padding,
                    onGoHome = { openTopLevel(Routes.HOME) },
                    onOpenScan = { navController.navigate(Routes.SCAN) },
                )
            }
        }

        composable(Routes.IDEAS) {
            TopLevelScaffold(
                selectedRoute = Routes.IDEAS,
                onNavigate = openTopLevel,
            ) { padding ->
                IdeasScreen(
                    contentPadding = padding,
                    onOpenRecipe = { id -> navController.navigate(Routes.recipe(id)) },
                    onGoHome = { openTopLevel(Routes.HOME) },
                )
            }
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenDiet = { navController.navigate(Routes.DIET) },
            )
        }

        composable(Routes.PROFILE) {
            TopLevelScaffold(
                selectedRoute = Routes.PROFILE,
                onNavigate = openTopLevel,
            ) { padding ->
                SettingsScreen(
                    onOpenDiet = { navController.navigate(Routes.DIET) },
                    contentPadding = padding,
                )
            }
        }

        composable(Routes.DIET) {
            OnboardingScreen(
                canSkip = false,
                onFinished = { navController.popBackStack() },
            )
        }

        composable(Routes.SCAN) {
            ScanScreen(
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.navigate(Routes.KITCHEN) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }

        composable(
            route = Routes.CONFIRM,
            arguments = listOf(
                navArgument(Routes.ARG_TEXT) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) {
            ConfirmScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.navigate(Routes.KITCHEN) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }

        composable(
            route = Routes.RECIPE,
            arguments = listOf(
                navArgument(Routes.ARG_RECIPE_ID) { type = NavType.StringType },
            ),
        ) {
            RecipeDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
