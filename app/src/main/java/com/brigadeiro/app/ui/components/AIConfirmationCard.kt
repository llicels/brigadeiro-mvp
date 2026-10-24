package com.brigadeiro.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brigadeiro.app.domain.model.IngredientDraft

/**
 * Editable row shown on the confirmation screen. The user can correct the name,
 * quantity and unit before anything is saved. Uncertainty is shown with a subtle
 * badge, never an alarming red.
 */
@Composable
fun AIConfirmationCard(
    draft: IngredientDraft,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var quantityText by remember(draft.name) {
        mutableStateOf(draft.quantity?.let { formatShort(it) } ?: "")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = ingredientCategoryIcon(draft.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(20.dp).height(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Ingrediente") },
                )
                BrigadeiroIconButton(
                    icon = Icons.Outlined.Close,
                    contentDescription = "Remover ${draft.name}",
                    onClick = onRemove,
                )
            }
            if (draft.uncertain) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Confira este item",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        quantityText = it
                        onQuantityChange(it)
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Qtd.") },
                )
                OutlinedTextField(
                    value = draft.unit.orEmpty(),
                    onValueChange = onUnitChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Unidade") },
                )
            }
        }
    }
}

private fun formatShort(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
