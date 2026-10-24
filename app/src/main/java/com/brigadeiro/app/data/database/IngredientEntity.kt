package com.brigadeiro.app.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ingredients",
    indices = [Index("normalizedName", unique = true), Index("category")],
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val normalizedName: String,
    val quantity: Double?,
    val unit: String?,
    val category: String,
    val createdAt: Long,
    val updatedAt: Long,
)
