package com.brigadeiro.app.di

import android.content.Context
import com.brigadeiro.app.ai.catalog.CatalogRecipeGenerator
import com.brigadeiro.app.ai.extractor.CompositeIngredientExtractor
import com.brigadeiro.app.ai.extractor.HeuristicIngredientExtractor
import com.brigadeiro.app.ai.extractor.LlmIngredientExtractor
import com.brigadeiro.app.ai.local.LocalAIProvider
import com.brigadeiro.app.ai.remote.RemoteAIProvider
import com.brigadeiro.app.ai.remote.RemoteRecipeGenerator
import com.brigadeiro.app.data.database.AppDatabase
import com.brigadeiro.app.data.local.DietRepositoryImpl
import com.brigadeiro.app.data.local.ModelManager
import com.brigadeiro.app.data.local.SettingsRepositoryImpl
import com.brigadeiro.app.data.repository.IngredientRepositoryImpl
import com.brigadeiro.app.domain.TimeProvider
import com.brigadeiro.app.domain.repository.DietRepository
import com.brigadeiro.app.domain.repository.IngredientRepository
import com.brigadeiro.app.domain.repository.SettingsRepository
import com.brigadeiro.app.domain.usecase.ConfirmIngredientsUseCase
import com.brigadeiro.app.domain.usecase.GenerateRecipeUseCase
import com.brigadeiro.app.domain.usecase.ParseIngredientsUseCase
import com.brigadeiro.app.domain.usecase.SuggestRecipesUseCase

/**
 * Manual dependency container. Small enough that a DI framework would be
 * overkill, but centralised so wiring lives in one place.
 */
class AppContainer(
    context: Context,
    database: AppDatabase = AppDatabase.build(context),
) {
    private val db: AppDatabase = database
    private val appContext = context.applicationContext

    val timeProvider: TimeProvider = TimeProvider { System.currentTimeMillis() }

    // Data
    val ingredientRepository: IngredientRepository = IngredientRepositoryImpl(db.ingredientDao())
    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(appContext)
    val dietRepository: DietRepository = DietRepositoryImpl(appContext)
    val modelManager: ModelManager = ModelManager(appContext)

    // AI providers — the rest of the app only sees the interfaces above them.
    val localAIProvider = LocalAIProvider(modelManager)
    val remoteAIProvider = RemoteAIProvider(
        configProvider = { settingsRepository.current() },
    )

    // Extraction: deterministic first, local model as an optional refiner.
    private val heuristicExtractor = HeuristicIngredientExtractor()
    private val llmExtractor = LlmIngredientExtractor(localAIProvider)
    val ingredientExtractor = CompositeIngredientExtractor(heuristicExtractor, llmExtractor)

    // Recipes: offline catalog by default, remote when configured.
    private val catalogRecipeGenerator = CatalogRecipeGenerator()
    val recipeGenerator = RemoteRecipeGenerator(remoteAIProvider, catalogRecipeGenerator)

    // Use cases
    val parseIngredients = ParseIngredientsUseCase(ingredientExtractor)
    val confirmIngredients = ConfirmIngredientsUseCase(ingredientRepository, timeProvider)
    val suggestRecipes = SuggestRecipesUseCase(recipeGenerator, dietRepository)
    val generateRecipe = GenerateRecipeUseCase(recipeGenerator, dietRepository)
}
