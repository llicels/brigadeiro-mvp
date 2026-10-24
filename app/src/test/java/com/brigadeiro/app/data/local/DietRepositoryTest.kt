package com.brigadeiro.app.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.brigadeiro.app.domain.model.DietType
import com.brigadeiro.app.domain.model.Restriction
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DietRepositoryTest {

    @Test
    fun savesProfileAndMarksOnboarded() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = DietRepositoryImpl(context)

        repository.save(
            com.brigadeiro.app.domain.model.DietProfile(
                dietType = DietType.Omnivore,
                restrictions = emptySet(),
                onboarded = false,
            ),
        )

        val profile = repository.current()
        assertThat(profile.dietType).isEqualTo(DietType.Omnivore)
        assertThat(profile.onboarded).isTrue()
        assertThat(profile.restrictions).isEmpty()
    }

    @Test
    fun savesAndReadsProfile() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = DietRepositoryImpl(context)

        repository.save(
            com.brigadeiro.app.domain.model.DietProfile(
                dietType = DietType.Pescatarian,
                restrictions = setOf(Restriction.Lactose, Restriction.Gluten),
                preferFreezerFriendly = true,
                onboarded = true,
            ),
        )

        val stored = repository.profile.first()
        assertThat(stored.dietType).isEqualTo(DietType.Pescatarian)
        assertThat(stored.restrictions)
            .containsExactly(Restriction.Lactose, Restriction.Gluten)
        assertThat(stored.preferFreezerFriendly).isTrue()
        assertThat(stored.onboarded).isTrue()
    }
}
