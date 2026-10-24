package com.brigadeiro.app.ui.screens.confirm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brigadeiro.app.di.BrigadeiroViewModelFactory
import com.brigadeiro.app.ui.components.AIConfirmationCard
import com.brigadeiro.app.ui.components.BrigadeiroButton
import com.brigadeiro.app.ui.components.BrigadeiroSecondaryButton
import com.brigadeiro.app.ui.components.EmptyState
import com.brigadeiro.app.ui.components.LoadingState
import com.brigadeiro.app.ui.components.TestTags
import com.brigadeiro.app.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ConfirmViewModel = viewModel(factory = BrigadeiroViewModelFactory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Confirmar ingredientes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Voltar",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.loading -> LoadingState(message = "\u2728 Entendendo o que você disse...")
                state.drafts.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = "Não identifiquei ingredientes",
                    message = "Tente falar um pouco mais devagar ou digite os ingredientes.",
                    action = {
                        BrigadeiroButton(text = "Tentar novamente", onClick = onBack)
                    },
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .testTag(TestTags.CONFIRM_LIST),
                        contentPadding = PaddingValues(
                            start = Spacing.screenHorizontal,
                            end = Spacing.screenHorizontal,
                            top = Spacing.sm,
                            bottom = Spacing.lg,
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        item {
                            Column {
                                Text(
                                    text = "\u2728 Entendi assim:",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                if (state.sourceText.isNotBlank()) {
                                    Spacer(Modifier.height(Spacing.xs))
                                    Text(
                                        text = "\"${state.sourceText}\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Spacer(Modifier.height(Spacing.md))
                            }
                        }
                        itemsIndexed(state.drafts) { index, draft ->
                            AIConfirmationCard(
                                draft = draft,
                                onNameChange = { viewModel.updateName(index, it) },
                                onQuantityChange = { viewModel.updateQuantity(index, it) },
                                onUnitChange = { viewModel.updateUnit(index, it) },
                                onRemove = { viewModel.remove(index) },
                            )
                        }
                        item {
                            Spacer(Modifier.height(Spacing.sm))
                            BrigadeiroSecondaryButton(
                                text = "Adicionar outro",
                                onClick = viewModel::addEmpty,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        BrigadeiroSecondaryButton(
                            text = "Editar",
                            onClick = onBack,
                            modifier = Modifier.weight(1f),
                        )
                        BrigadeiroButton(
                            text = "Confirmar",
                            onClick = viewModel::confirm,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(TestTags.CONFIRM_BUTTON),
                        )
                    }
                }
            }
        }
    }
}
