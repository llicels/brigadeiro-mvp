package com.brigadeiro.app.ui.screens.ideas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brigadeiro.app.di.BrigadeiroViewModelFactory
import com.brigadeiro.app.ui.components.BrigadeiroButton
import com.brigadeiro.app.ui.components.EmptyState
import com.brigadeiro.app.ui.components.ErrorState
import com.brigadeiro.app.ui.components.LoadingState
import com.brigadeiro.app.ui.components.RecipeCard
import com.brigadeiro.app.ui.components.SectionHeader
import com.brigadeiro.app.ui.components.TestTags
import com.brigadeiro.app.ui.theme.Spacing

@Composable
fun IdeasScreen(
    contentPadding: PaddingValues,
    onOpenRecipe: (String) -> Unit,
    onGoHome: () -> Unit,
    viewModel: IdeasViewModel = viewModel(factory = BrigadeiroViewModelFactory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding() + Spacing.lg,
                bottom = contentPadding.calculateBottomPadding(),
            ),
    ) {
        Column(modifier = Modifier.padding(horizontal = Spacing.screenHorizontal)) {
            SectionHeader(title = "Ideias")
            Text(
                text = "Receitas que combinam com o que você já tem.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(Spacing.sm))

        TimeFilters(
            selected = state.maxMinutes,
            onSelected = viewModel::setTimeFilter,
        )
        Spacer(Modifier.height(Spacing.sm))

        when {
            state.loading -> LoadingState(message = "\uD83C\uDF73 Pensando no que combina...")
            state.emptyKitchen -> EmptyState(
                icon = Icons.Outlined.Kitchen,
                title = "Sua cozinha ainda está vazia",
                message = "Adicione alguns ingredientes e eu mostro o que dá pra fazer.",
                action = { BrigadeiroButton(text = "Adicionar ingredientes", onClick = onGoHome) },
            )
            state.error -> ErrorState(
                title = "Não foi possível gerar novas sugestões agora.",
                message = "Seus ingredientes continuam disponíveis offline.",
                action = { BrigadeiroButton(text = "Tentar novamente", onClick = viewModel::refresh) },
            )
            state.suggestions.isEmpty() -> EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = "Ainda não achei uma combinação",
                message = "Adicione mais alguns ingredientes para eu sugerir receitas.",
                action = { BrigadeiroButton(text = "Ver minha cozinha", onClick = onGoHome) },
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(TestTags.IDEAS_LIST),
                contentPadding = PaddingValues(
                    start = Spacing.screenHorizontal,
                    end = Spacing.screenHorizontal,
                    top = Spacing.xs,
                    bottom = Spacing.xxl,
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(state.suggestions, key = { it.id }) { suggestion ->
                    RecipeCard(
                        suggestion = suggestion,
                        onClick = { onOpenRecipe(suggestion.id) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private data class TimeFilterOption(val minutes: Int?, val label: String)

private val timeFilterOptions = listOf(
    TimeFilterOption(null, "Qualquer"),
    TimeFilterOption(10, "Até 10 min"),
    TimeFilterOption(20, "Até 20 min"),
    TimeFilterOption(30, "Até 30 min"),
)

@Composable
private fun TimeFilters(
    selected: Int?,
    onSelected: (Int?) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTags.IDEAS_FILTER),
        contentPadding = PaddingValues(horizontal = Spacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(timeFilterOptions.size) { index ->
            val option = timeFilterOptions[index]
            FilterChip(
                selected = selected == option.minutes,
                onClick = { onSelected(option.minutes) },
                label = { Text(option.label) },
            )
        }
    }
}
