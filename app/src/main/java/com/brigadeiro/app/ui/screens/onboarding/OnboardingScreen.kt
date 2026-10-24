package com.brigadeiro.app.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brigadeiro.app.di.BrigadeiroViewModelFactory
import com.brigadeiro.app.domain.model.DietProfile
import com.brigadeiro.app.domain.model.DietType
import com.brigadeiro.app.domain.model.Equipment
import com.brigadeiro.app.domain.model.Restriction
import com.brigadeiro.app.ui.components.BrigadeiroButton
import com.brigadeiro.app.ui.components.BrigadeiroTextButton
import com.brigadeiro.app.ui.components.LoadingState
import com.brigadeiro.app.ui.components.dietTypeIcon
import com.brigadeiro.app.ui.components.equipmentIcon
import com.brigadeiro.app.ui.theme.Spacing

private const val STEP_COUNT = 4

@Composable
fun OnboardingScreen(
    canSkip: Boolean,
    onFinished: () -> Unit,
    viewModel: DietViewModel = viewModel(factory = BrigadeiroViewModelFactory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var step by remember { mutableIntStateOf(0) }
    var edited by remember { mutableStateOf<DietProfile?>(null) }

    LaunchedEffect(state.loading) {
        if (!state.loading && edited == null) edited = state.profile
    }
    LaunchedEffect(state.saved) {
        if (state.saved) onFinished()
    }

    val profile = edited
    if (profile == null) {
        LoadingState(message = "Preparando tudo...")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Brigadeiro",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            BrigadeiroTextButton(
                text = if (canSkip) "Pular" else "Cancelar",
                onClick = {
                    if (canSkip) viewModel.skip() else onFinished()
                },
            )
        }

        Spacer(Modifier.height(Spacing.lg))
        LinearProgressIndicator(
            progress = { (step + 1) / STEP_COUNT.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(Modifier.height(Spacing.lg))

        when (step) {
            0 -> {
                StepTitle(
                    title = "Como você come?",
                    subtitle = "Uso isso para sugerir receitas que combinam com você.",
                )
                Spacer(Modifier.height(Spacing.lg))
                DietType.entries.forEach { type ->
                    DietTypeCard(
                        type = type,
                        selected = profile.dietType == type,
                        onClick = { edited = profile.copy(dietType = type) },
                    )
                    Spacer(Modifier.height(Spacing.sm))
                }
            }

            1 -> {
                StepTitle(
                    title = "Você evita algo?",
                    subtitle = "Escolha o que quiser. Pode mudar depois nos Ajustes.",
                )
                Spacer(Modifier.height(Spacing.lg))
                Restriction.entries.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        row.forEach { restriction ->
                            FilterChip(
                                selected = restriction in profile.restrictions,
                                onClick = {
                                    val next = profile.restrictions.toMutableSet()
                                    if (restriction in next) next.remove(restriction) else next.add(restriction)
                                    edited = profile.copy(restrictions = next)
                                },
                                label = { Text(restriction.label) },
                                colors = FilterChipDefaults.filterChipColors(),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(Spacing.sm))
                }
            }

            2 -> {
                StepTitle(
                    title = "O que tem na sua cozinha?",
                    subtitle = "Marque o que você usa. Escondo receitas que precisam de " +
                        "equipamento que você não tem.",
                )
                Spacer(Modifier.height(Spacing.lg))
                Equipment.entries.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        row.forEach { equipment ->
                            FilterChip(
                                selected = equipment in profile.equipment,
                                onClick = {
                                    val next = profile.equipment.toMutableSet()
                                    if (equipment in next) {
                                        next.remove(equipment)
                                    } else {
                                        next.add(equipment)
                                    }
                                    edited = profile.copy(equipment = next)
                                },
                                label = { Text(equipment.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = equipmentIcon(equipment),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(Spacing.sm))
                }
            }

            else -> {
                StepTitle(
                    title = "Precisa de receitas pra congelar?",
                    subtitle = "Quando ativo, essas receitas aparecem primeiro nas ideias.",
                )
                Spacer(Modifier.height(Spacing.lg))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.card),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "\uD83E\uDDCA Priorizar congeláveis",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Boa para cozinhar uma vez e comer na semana.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = profile.preferFreezerFriendly,
                            onCheckedChange = {
                                edited = profile.copy(preferFreezerFriendly = it)
                            },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(Spacing.xxl))

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            if (step > 0) {
                BrigadeiroTextButton(
                    text = "Voltar",
                    onClick = { step-- },
                    modifier = Modifier.weight(1f),
                )
            }
            BrigadeiroButton(
                text = if (step < STEP_COUNT - 1) "Continuar" else "Começar",
                onClick = {
                    if (step < STEP_COUNT - 1) {
                        step++
                    } else {
                        viewModel.save(profile)
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(Spacing.xl))
    }
}

@Composable
private fun StepTitle(title: String, subtitle: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(Modifier.height(Spacing.xs))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DietTypeCard(
    type: DietType,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.card),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = dietTypeIcon(type),
                    contentDescription = null,
                    tint = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = type.label,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Spacer(Modifier.weight(1f))
        }
    }
}
