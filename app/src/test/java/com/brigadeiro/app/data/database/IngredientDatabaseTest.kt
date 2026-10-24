package com.brigadeiro.app.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.brigadeiro.app.data.repository.IngredientRepositoryImpl
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.IngredientCategory
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IngredientDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: IngredientRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = IngredientRepositoryImpl(db.ingredientDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun ingredient(name: String, quantity: Double? = null) = Ingredient(
        name = name,
        normalizedName = com.brigadeiro.app.domain.model.TextNormalizer.normalize(name),
        quantity = quantity,
        unit = if (quantity != null) "un" else null,
        category = IngredientCategory.Produce,
        createdAt = 1L,
        updatedAt = 1L,
    )

    @Test
    fun insertsAndReadsBack() = runTest {
        val id = repository.add(ingredient("Tomate", 2.0))

        val stored = repository.getById(id)
        assertThat(stored).isNotNull()
        assertThat(stored!!.name).isEqualTo("Tomate")
        assertThat(stored.normalizedName).isEqualTo("tomate")
        assertThat(stored.quantity).isEqualTo(2.0)
    }

    @Test
    fun updatingChangesQuantity() = runTest {
        val id = repository.add(ingredient("Arroz"))
        val stored = repository.getById(id)!!

        repository.update(stored.copy(quantity = 5.0))

        assertThat(repository.getById(id)!!.quantity).isEqualTo(5.0)
    }

    @Test
    fun deletingRemovesIngredient() = runTest {
        val id = repository.add(ingredient("Batata"))

        repository.delete(id)

        assertThat(repository.getById(id)).isNull()
        assertThat(repository.count()).isEqualTo(0)
    }

    @Test
    fun observesInsertedItems() = runTest {
        repository.add(ingredient("Cebola"))

        val observed = repository.observeAll().first()
        assertThat(observed.map { it.name }).containsExactly("Cebola")
    }
}
