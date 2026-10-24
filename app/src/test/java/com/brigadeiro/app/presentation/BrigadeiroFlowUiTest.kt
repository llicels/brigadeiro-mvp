package com.brigadeiro.app.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.brigadeiro.app.TestBrigadeiroApp
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.IngredientCategory
import com.brigadeiro.app.domain.model.TextNormalizer
import com.brigadeiro.app.ui.components.TestTags
import com.brigadeiro.app.ui.navigation.BrigadeiroNavHost
import com.brigadeiro.app.ui.theme.BrigadeiroTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(application = TestBrigadeiroApp::class, sdk = [33], qualifiers = "w400dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BrigadeiroFlowUiTest {

    @get:Rule
    val compose = createComposeRule()

    private fun seed(vararg names: String) = runBlocking {
        val app = ApplicationProvider.getApplicationContext<TestBrigadeiroApp>()
        names.forEach { name ->
            app.container.ingredientRepository.add(
                Ingredient(
                    name = name,
                    normalizedName = TextNormalizer.normalize(name),
                    category = IngredientCategory.Produce,
                    createdAt = 1L,
                    updatedAt = 1L,
                ),
            )
        }
    }

    private fun start() {
        compose.setContent {
            BrigadeiroTheme { BrigadeiroNavHost() }
        }
    }

    @Test
    fun typingIngredientsConfirmsAndLandsInKitchen() {
        start()

        compose.onNodeWithTag(TestTags.HOME_INPUT)
            .performTextInput("tenho arroz e dois tomates")
        compose.onNodeWithTag(TestTags.HOME_SUBMIT).performClick()

        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag(TestTags.CONFIRM_BUTTON).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Confirmar").assertIsDisplayed()

        compose.onNodeWithTag(TestTags.CONFIRM_BUTTON).performClick()

        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag(TestTags.KITCHEN_LIST).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("arroz").assertIsDisplayed()
    }

    @Test
    fun removingAnIngredientFromKitchen() {
        seed("Tomate")
        start()

        compose.onNodeWithText("Cozinha").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("Tomate").fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithContentDescription("Remover Tomate").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("Tomate").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun ideasOpensARecipe() {
        seed("arroz", "tomate", "queijo")
        start()

        compose.onNodeWithText("Ideias").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("Arroz cremoso de tomate").fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithText("Arroz cremoso de tomate").performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("Modo de preparo").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Modo de preparo").assertExists()
    }
}
