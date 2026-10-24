package com.brigadeiro.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BreakfastDining
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Microwave
import androidx.compose.material.icons.outlined.OutdoorGrill
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.RiceBowl
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material.icons.outlined.SoupKitchen
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector
import com.brigadeiro.app.domain.model.DietType
import com.brigadeiro.app.domain.model.Equipment
import com.brigadeiro.app.domain.model.IngredientCategory

fun ingredientCategoryIcon(category: IngredientCategory): ImageVector = when (category) {
    IngredientCategory.Produce -> Icons.Outlined.Eco
    IngredientCategory.Grains -> Icons.Outlined.RiceBowl
    IngredientCategory.Dairy -> Icons.Outlined.Icecream
    IngredientCategory.Proteins -> Icons.Outlined.SetMeal
    IngredientCategory.Grocery -> Icons.Outlined.ShoppingBasket
    IngredientCategory.Others -> Icons.Outlined.Category
}

fun equipmentIcon(equipment: Equipment): ImageVector = when (equipment) {
    Equipment.Stove -> Icons.Outlined.LocalFireDepartment
    Equipment.Oven -> Icons.Outlined.Whatshot
    Equipment.Microwave -> Icons.Outlined.Microwave
    Equipment.AirFryer -> Icons.Outlined.OutdoorGrill
    Equipment.Blender -> Icons.Outlined.Kitchen
    Equipment.SandwichMaker -> Icons.Outlined.BreakfastDining
    Equipment.PressureCooker -> Icons.Outlined.SoupKitchen
    Equipment.Mixer -> Icons.Outlined.Kitchen
}

fun dietTypeIcon(dietType: DietType): ImageVector = when (dietType) {
    DietType.Omnivore -> Icons.Outlined.Restaurant
    DietType.Pescatarian -> Icons.Outlined.SetMeal
    DietType.Vegetarian -> Icons.Outlined.Eco
    DietType.Vegan -> Icons.Outlined.Spa
}
