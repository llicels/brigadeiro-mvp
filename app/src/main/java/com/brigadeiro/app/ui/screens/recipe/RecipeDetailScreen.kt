package com.brigadeiro.app.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brigadeiro.app.di.BrigadeiroViewModelFactory
import com.brigadeiro.app.domain.model.Recipe
import com.brigadeiro.app.ui.components.BrigadeiroButton
import com.brigadeiro.app.ui.components.ErrorState
import com.brigadeiro.app.ui.components.LoadingState
import com.brigadeiro.app.ui.components.RecipeImage
import com.brigadeiro.app.ui.components.RoundedSection
import com.brigadeiro.app.ui.components.TestTags
import com.brigadeiro.app.ui.theme.Spacing
import com.brigadeiro.app.ui.util.costLabel
import com.brigadeiro.app.ui.util.timeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    onBack: () -> Unit,
    viewModel: RecipeDetailViewModel = viewModel(factory = BrigadeiroViewModelFactory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var cooking by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(emptySet<Int>()) }

    val recipe = state.recipe
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        bottomBar = {
            if (recipe != null) {
                RecipeBottomBar(
                    cost = recipe.estimatedCostBRL,
                    cooking = cooking,
                    onToggle = {
                        cooking = !cooking
                        if (!cooking) completed = emptySet()
                    },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .testTag(TestTags.RECIPE_DETAIL),
        ) {
            when {
                state.loading -> LoadingState(message = "\uD83C\uDF73 Montando a receita...")
                state.error || recipe == null -> ErrorState(
                    title = "Não consegui abrir essa receita",
                    message = "Tente novamente em instantes.",
                    action = { BrigadeiroButton(text = "Tentar novamente", onClick = viewModel::load) },
                )
                else -> RecipeContent(
                    recipe = recipe,
                    cooking = cooking,
                    completed = completed,
                    onToggleStep = { order ->
                        completed = if (order in completed) completed - order else completed + order
                    },
                )
            }
        }
    }
}

@Composable
private fun RecipeContent(
    recipe: Recipe,
    cooking: Boolean,
    completed: Set<Int>,
    onToggleStep: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero with overlaid title
        Box {
            RecipeImage(
                slug = recipe.imageSlug,
                emoji = recipe.imageEmoji,
                imageUrl = recipe.imageUrl,
                height = 240.dp,
                emojiSize = 84.sp,
                scrimBottom = true,
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Spacing.card),
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                )
                Spacer(Modifier.height(Spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    HeroPill(
                        text = timeLabel(recipe.timeMinutes, recipe.activeTimeMinutes),
                        leadingIcon = true,
                    )
                    HeroPill(text = recipe.difficulty)
                }
            }
        }

        Column(modifier = Modifier.padding(Spacing.screenHorizontal)) {
            Spacer(Modifier.height(Spacing.lg))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${recipe.servings} porções",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (recipe.freezerFriendly) {
                    Spacer(Modifier.width(Spacing.md))
                    Text(
                        text = "\u2744\uFE0F Congelável",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (recipe.requiredEquipment.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "Você vai precisar de: " +
                        recipe.requiredEquipment.joinToString(", ") { it.label },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(Spacing.md))

            val available = recipe.ingredients.filter { it.available }
            if (available.isNotEmpty()) {
                RoundedSection {
                    Text(
                        text = "Você já tem",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    available.forEach { ingredient ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.width(16.dp).height(16.dp),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                            Spacer(Modifier.width(Spacing.xs))
                            Text(
                                text = ingredient.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.md))
            }

            RoundedSection {
                Text(
                    text = "Ingredientes",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(Spacing.sm))
                recipe.ingredients.forEach { ingredient ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        Icon(
                            imageVector = if (ingredient.available) {
                                Icons.Outlined.CheckCircle
                            } else {
                                Icons.Outlined.RadioButtonUnchecked
                            },
                            contentDescription = if (ingredient.available) "Disponível" else "Falta",
                            modifier = Modifier.width(18.dp).height(18.dp),
                            tint = if (ingredient.available) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = buildString {
                                append(ingredient.name)
                                if (!ingredient.quantity.isNullOrBlank()) {
                                    append(" · ")
                                    append(ingredient.quantity)
                                }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            if (recipe.substitutions.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.md))
                RoundedSection {
                    Text(
                        text = "Alternativas",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "Ideias de troca, se você preferir. São opções, não regras.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    recipe.substitutions.forEach { substitution ->
                        Text(
                            text = "\u2022 ${substitution.ingredient} \u2192 " +
                                substitution.alternatives.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))
            RoundedSection {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Modo de preparo",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                    )
                    if (cooking) {
                        Text(
                            text = "${completed.size} de ${recipe.steps.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.sm))
                recipe.steps.forEach { step ->
                    val isDone = step.order in completed
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = cooking) { onToggleStep(step.order) }
                            .padding(vertical = Spacing.sm),
                    ) {
                        Icon(
                            imageVector = if (isDone) {
                                Icons.Outlined.CheckCircle
                            } else {
                                Icons.Outlined.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            modifier = Modifier.width(20.dp).height(20.dp),
                            tint = if (isDone) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = step.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun HeroPill(
    text: String,
    leadingIcon: Boolean = false,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0xCC000000),
        contentColor = Color.White,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon) {
                Icon(
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.width(13.dp).height(13.dp),
                    tint = Color.White,
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun RecipeBottomBar(
    cost: Double,
    cooking: Boolean,
    onToggle: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (cost > 0.0) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = costLabel(cost),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "estimativa",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
            BrigadeiroButton(
                text = if (cooking) "Encerrar preparo" else "Começar receita",
                onClick = onToggle,
            )
        }
    }
}
