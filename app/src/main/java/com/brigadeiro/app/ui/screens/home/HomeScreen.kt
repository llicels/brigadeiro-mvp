package com.brigadeiro.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.RiceBowl
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brigadeiro.app.di.BrigadeiroViewModelFactory
import com.brigadeiro.app.domain.model.IngredientCategory
import com.brigadeiro.app.ui.components.BrigadeiroButton
import com.brigadeiro.app.ui.components.BrigadeiroSecondaryButton
import com.brigadeiro.app.ui.components.BrigadeiroTextButton
import com.brigadeiro.app.ui.components.CategoryItem
import com.brigadeiro.app.ui.components.CategoryRow
import com.brigadeiro.app.ui.components.DietSummaryChip
import com.brigadeiro.app.ui.components.EmptyState
import com.brigadeiro.app.ui.components.IngredientChip
import com.brigadeiro.app.ui.components.ListeningIndicator
import com.brigadeiro.app.ui.components.LoadingState
import com.brigadeiro.app.ui.components.PromoBanner
import com.brigadeiro.app.ui.components.RecipeCard
import com.brigadeiro.app.ui.components.RoundedSection
import com.brigadeiro.app.ui.components.SearchField
import com.brigadeiro.app.ui.components.SectionHeader
import com.brigadeiro.app.ui.components.TestTags
import com.brigadeiro.app.ui.components.ingredientCategoryIcon
import com.brigadeiro.app.ui.theme.Spacing
import com.brigadeiro.app.ui.voice.rememberVoiceInput

@Composable
fun HomeScreen(
    onOpenConfirm: (String) -> Unit,
    onOpenRecipe: (String) -> Unit,
    onOpenKitchen: () -> Unit,
    onOpenIdeas: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDiet: () -> Unit,
    onOpenScan: () -> Unit,
    contentPadding: PaddingValues,
    viewModel: HomeViewModel = viewModel(factory = BrigadeiroViewModelFactory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var text by rememberSaveable { mutableStateOf("") }

    val voice = rememberVoiceInput { transcription -> onOpenConfirm(transcription) }

    val categories = IngredientCategory.entries.map { category ->
        CategoryItem(
            id = category.name,
            icon = ingredientCategoryIcon(category),
            label = category.label,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = contentPadding.calculateTopPadding()),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sua cozinha",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = state.greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        Icons.Outlined.PersonOutline,
                        contentDescription = "Perfil e ajustes",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Search / ingredient input
            Column(modifier = Modifier.padding(horizontal = Spacing.screenHorizontal)) {
                SearchField(
                    value = text,
                    onValueChange = { text = it },
                    tag = TestTags.HOME_INPUT,
                    placeholder = "O que você tem em casa?",
                    minLines = 1,
                    maxLines = 3,
                    shape = MaterialTheme.shapes.large,
                    leading = {
                        Icon(
                            Icons.Outlined.LocalDining,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailing = {
                        Row {
                            IconButton(onClick = onOpenScan) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = "Escanear nota",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = voice.onMicClick) {
                                Icon(
                                    imageVector = if (voice.isListening) {
                                        Icons.Filled.Stop
                                    } else {
                                        Icons.Filled.Mic
                                    },
                                    contentDescription = if (voice.isListening) {
                                        "Parar de ouvir"
                                    } else {
                                        "Falar com o Brigadeiro"
                                    },
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    },
                )

                if (voice.isListening) {
                    Spacer(Modifier.height(Spacing.sm))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        ListeningIndicator()
                    }
                }

                voice.errorMessage?.let { message ->
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                if (text.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.md))
                    BrigadeiroButton(
                        text = "Organizar meus ingredientes",
                        onClick = { onOpenConfirm(text) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TestTags.HOME_SUBMIT),
                    )
                }

                if (state.dietSummary.isNotBlank()) {
                    Spacer(Modifier.height(Spacing.md))
                    DietSummaryChip(
                        summary = state.dietSummary,
                        onClick = onOpenDiet,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))

            // Categories
            CategoryRow(
                items = categories,
                onSelect = { onOpenKitchen() },
            )

            Spacer(Modifier.height(Spacing.lg))

            // Promo banner
            PromoBanner(
                title = "O que dá pra fazer agora?",
                subtitle = "Veja ideias com o que já está na sua cozinha.",
                icon = Icons.Outlined.LocalDining,
                onClick = onOpenIdeas,
                modifier = Modifier.padding(horizontal = Spacing.screenHorizontal),
            )

            Spacer(Modifier.height(Spacing.xl))

            // Suggestions carousel
            Column(modifier = Modifier.padding(horizontal = Spacing.screenHorizontal)) {
                SectionHeader(
                    title = "Combina com você",
                    trailing = {
                        BrigadeiroTextButton(text = "Ver ideias", onClick = onOpenIdeas)
                    },
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            when {
                state.loading -> LoadingState(message = "Organizando sua cozinha...")
                state.suggestions.isEmpty() -> Text(
                    text = "Assim que você adicionar ingredientes, mostro ideias de receitas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.screenHorizontal),
                )
                else -> LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.screenHorizontal),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    items(state.suggestions, key = { it.id }) { suggestion ->
                        RecipeCard(
                            suggestion = suggestion,
                            onClick = { onOpenRecipe(suggestion.id) },
                            modifier = Modifier.width(230.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            // Minha cozinha (estoque)
            Column(modifier = Modifier.padding(horizontal = Spacing.screenHorizontal)) {
                SectionHeader(
                    title = "Minha cozinha",
                    trailing = {
                        BrigadeiroTextButton(text = "Ver tudo", onClick = onOpenKitchen)
                    },
                )
                Spacer(Modifier.height(Spacing.sm))
                RoundedSection {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (state.ingredientCount) {
                                    0 -> "Estoque vazio"
                                    1 -> "1 ingrediente no estoque"
                                    else -> "${state.ingredientCount} ingredientes no estoque"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Adicione por texto, voz ou foto da nota.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.Kitchen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    if (state.preview.isNotEmpty()) {
                        Spacer(Modifier.height(Spacing.sm))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            items(state.preview) { ingredient ->
                                IngredientChip(
                                    icon = ingredientCategoryIcon(ingredient.category),
                                    label = ingredient.name,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.md))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        BrigadeiroSecondaryButton(
                            text = "Escanear nota",
                            onClick = onOpenScan,
                            modifier = Modifier.weight(1f),
                        )
                        BrigadeiroButton(
                            text = "Ver cozinha",
                            onClick = onOpenKitchen,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}
