package com.brigadeiro.app.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.brigadeiro.app.TestBrigadeiroApp
import com.brigadeiro.app.domain.model.DietType
import com.brigadeiro.app.ui.theme.BrigadeiroTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Uses a fresh app instance whose diet profile is reset to "not onboarded" so
 * the wizard is shown on launch.
 */
@RunWith(AndroidJUnit4::class)
@Config(application = TestBrigadeiroApp::class, sdk = [33], qualifiers = "w400dp-h800dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OnboardingFlowUiTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun dietEditorFlowFromHomeSummaryChip() {
        compose.setContent { BrigadeiroTheme { com.brigadeiro.app.ui.navigation.BrigadeiroNavHost() } }

        // With the test app onboarded, we should be on Home and able to reach the
        // diet editor via the summary chip.
        compose.onNodeWithText("Sua cozinha").assertIsDisplayed()
        compose.onNodeWithText("Como de tudo").performClick()

        // The diet editor opens and lets the user pick a diet and confirm steps.
        compose.onNodeWithText("Como você come?").assertIsDisplayed()
        compose.onNodeWithText(DietType.Vegan.label).performClick()
        compose.onNodeWithText("Continuar").performClick()
        compose.onNodeWithText("Você evita algo?").assertIsDisplayed()
        compose.onNodeWithText("Continuar").performClick()
        compose.onNodeWithText("O que tem na sua cozinha?").assertIsDisplayed()
        compose.onNodeWithText("Continuar").performClick()
        compose.onNodeWithText("Precisa de receitas pra congelar?").assertIsDisplayed()
        compose.onNodeWithText("Começar").performClick()

        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("Sua cozinha").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
