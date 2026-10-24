package com.brigadeiro.app.ui.screens.kitchen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.IngredientCategory
import com.brigadeiro.app.ui.components.TestTags
import com.brigadeiro.app.ui.theme.Spacing

@Composable
fun IngredientEditorDialog(
    initial: Ingredient?,
    onDismiss: () -> Unit,
    onSave: (name: String, quantity: Double?, unit: String?, category: IngredientCategory) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var quantity by remember {
        mutableStateOf(
            initial?.quantity?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                .orEmpty(),
        )
    }
    var unit by remember { mutableStateOf(initial?.unit.orEmpty()) }
    var category by remember { mutableStateOf(initial?.category ?: IngredientCategory.Others) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initial == null) "Adicionar ingrediente" else "Editar ingrediente")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.INGREDIENT_NAME_FIELD),
                )
                Spacer(Modifier.height(Spacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Qtd.") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unidade") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(Spacing.md))
                Text(
                    text = "Categoria",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.xs))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    IngredientCategory.entries.forEach { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { category = option },
                            label = { Text(option.label) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        name,
                        quantity.replace(',', '.').trim().toDoubleOrNull(),
                        unit.trim().takeIf { it.isNotEmpty() },
                        category,
                    )
                },
                enabled = name.trim().length >= 2,
                modifier = Modifier.testTag(TestTags.INGREDIENT_SAVE),
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
