package com.brigadeiro.app.ui.screens.kitchen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brigadeiro.app.di.BrigadeiroViewModelFactory
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.IngredientCategory
import com.brigadeiro.app.ui.components.BrigadeiroButton
import com.brigadeiro.app.ui.components.BrigadeiroSecondaryButton
import com.brigadeiro.app.ui.components.EmptyState
import com.brigadeiro.app.ui.components.IngredientCard
import com.brigadeiro.app.ui.components.LoadingState
import com.brigadeiro.app.ui.components.SearchField
import com.brigadeiro.app.ui.components.SectionHeader
import com.brigadeiro.app.ui.components.TestTags
import com.brigadeiro.app.ui.theme.Spacing

@Composable
fun KitchenScreen(
    contentPadding: PaddingValues,
    onGoHome: () -> Unit,
    onOpenScan: () -> Unit,
    viewModel: KitchenViewModel = viewModel(factory = BrigadeiroViewModelFactory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Ingredient?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding()),
        ) {
            Column(modifier = Modifier.padding(horizontal = Spacing.screenHorizontal)) {
                SectionHeader(
                    title = "Minha Cozinha",
                    trailing = {
                        BrigadeiroSecondaryButton(
                            text = "Escanear nota",
                            onClick = onOpenScan,
                        )
                    },
                )
                Text(
                    text = when (state.all.size) {
                        0 -> "Seu estoque está vazio"
                        1 -> "1 ingrediente no estoque"
                        else -> "${state.all.size} ingredientes no estoque"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.sm))
                SearchField(
                    value = state.query,
                    onValueChange = viewModel::setQuery,
                    modifier = Modifier.fillMaxWidth(),
                    tag = TestTags.KITCHEN_SEARCH,
                    placeholder = "Buscar ingrediente",
                    leading = { Icon(Icons.Outlined.Search, contentDescription = null) },
                )
                Spacer(Modifier.height(Spacing.sm))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    item {
                        FilterChip(
                            selected = state.category == null,
                            onClick = { viewModel.setCategory(null) },
                            label = { Text("Todos") },
                        )
                    }
                    items(IngredientCategory.entries.toList(), key = { it.name }) { category ->
                        FilterChip(
                            selected = state.category == category,
                            onClick = { viewModel.setCategory(category) },
                            label = { Text(category.label) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            when {
                state.loading -> LoadingState(message = "Organizando sua cozinha...")
                state.all.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.Kitchen,
                    title = "Sua cozinha está vazia por aqui",
                    message = "Conte pra mim o que você tem e eu começo a organizar.",
                    action = {
                        BrigadeiroButton(
                            text = "Adicionar ingredientes",
                            onClick = onGoHome,
                        )
                    },
                )
                state.visible.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = "Nenhum ingrediente encontrado",
                    message = "Tente outra busca ou selecione outra categoria.",
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(TestTags.KITCHEN_LIST),
                    contentPadding = PaddingValues(
                        start = Spacing.screenHorizontal,
                        end = Spacing.screenHorizontal,
                        bottom = contentPadding.calculateBottomPadding() + 96.dp,
                    ),
                ) {
                    items(state.visible, key = { it.id }) { ingredient ->
                        IngredientCard(
                            ingredient = ingredient,
                            onEdit = {
                                editing = ingredient
                                showEditor = true
                            },
                            onDelete = { viewModel.delete(ingredient.id) },
                            conflicting = ingredient.id in state.conflictingIds,
                        )
                        Spacer(Modifier.height(Spacing.sm))
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                editing = null
                showEditor = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = Spacing.screenHorizontal,
                    bottom = contentPadding.calculateBottomPadding() + Spacing.lg,
                )
                .testTag(TestTags.KITCHEN_ADD),
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Adicionar") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    }

    if (showEditor) {
        IngredientEditorDialog(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = { name, quantity, unit, category ->
                val current = editing
                if (current == null) {
                    viewModel.addManual(name, quantity, unit, category)
                } else {
                    viewModel.update(
                        current.copy(
                            name = name.trim(),
                            normalizedName = com.brigadeiro.app.domain.model.TextNormalizer.normalize(name),
                            quantity = quantity,
                            unit = unit,
                            category = category,
                        ),
                    )
                }
                showEditor = false
            },
        )
    }
}
