package com.brigadeiro.app.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.brigadeiro.app.ui.screens.confirm.ConfirmViewModel
import com.brigadeiro.app.ui.screens.home.HomeViewModel
import com.brigadeiro.app.ui.screens.onboarding.DietViewModel
import com.brigadeiro.app.ui.screens.ideas.IdeasViewModel
import com.brigadeiro.app.ui.screens.kitchen.KitchenViewModel
import com.brigadeiro.app.ui.screens.recipe.RecipeDetailViewModel
import com.brigadeiro.app.ui.screens.scan.ScanViewModel
import com.brigadeiro.app.ui.screens.settings.SettingsViewModel

/**
 * One factory for the whole app. ViewModels pull what they need from the
 * [AppContainer]; screens with navigation arguments get them through the
 * automatically provided [androidx.lifecycle.SavedStateHandle].
 */
val BrigadeiroViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
    initializer {
        HomeViewModel(
            ingredientRepository = container().ingredientRepository,
            dietRepository = container().dietRepository,
            suggestRecipes = container().suggestRecipes,
            timeProvider = container().timeProvider,
        )
    }
    initializer {
        KitchenViewModel(
            repository = container().ingredientRepository,
            dietRepository = container().dietRepository,
            timeProvider = container().timeProvider,
        )
    }
    initializer {
        ConfirmViewModel(
            parseIngredients = container().parseIngredients,
            confirmIngredients = container().confirmIngredients,
            savedStateHandle = createSavedStateHandle(),
        )
    }
    initializer {
        IdeasViewModel(
            ingredientRepository = container().ingredientRepository,
            suggestRecipes = container().suggestRecipes,
        )
    }
    initializer {
        RecipeDetailViewModel(
            ingredientRepository = container().ingredientRepository,
            suggestRecipes = container().suggestRecipes,
            generateRecipe = container().generateRecipe,
            savedStateHandle = createSavedStateHandle(),
        )
    }
    initializer {
        SettingsViewModel(
            settingsRepository = container().settingsRepository,
            dietRepository = container().dietRepository,
            modelManager = container().modelManager,
        )
    }
    initializer {
        DietViewModel(dietRepository = container().dietRepository)
    }
    initializer {
        ScanViewModel(confirmIngredients = container().confirmIngredients)
    }
}

private fun CreationExtras.container(): AppContainer =
    (this[APPLICATION_KEY] as AppContainerProvider).container
