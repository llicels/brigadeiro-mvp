package com.brigadeiro.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.brigadeiro.app.domain.model.DietProfile
import com.brigadeiro.app.domain.model.DietType
import com.brigadeiro.app.domain.model.Equipment
import com.brigadeiro.app.domain.model.Restriction
import com.brigadeiro.app.domain.repository.DietRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dietDataStore by preferencesDataStore(name = "brigadeiro_diet")

class DietRepositoryImpl(
    private val context: Context,
) : DietRepository {

    override val profile: Flow<DietProfile> = context.dietDataStore.data.map { prefs ->
        DietProfile(
            dietType = prefs[KEY_DIET_TYPE]
                ?.let { runCatching { DietType.valueOf(it) }.getOrNull() }
                ?: DietType.Omnivore,
            restrictions = prefs[KEY_RESTRICTIONS]
                ?.mapNotNull { runCatching { Restriction.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: emptySet(),
            preferFreezerFriendly = prefs[KEY_FREEZER] ?: false,
            equipment = prefs[KEY_EQUIPMENT]
                ?.mapNotNull { runCatching { Equipment.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?: Equipment.defaults,
            onboarded = prefs[KEY_ONBOARDED] ?: false,
        )
    }

    override suspend fun current(): DietProfile = profile.first()

    override suspend fun save(profile: DietProfile) {
        context.dietDataStore.edit { prefs ->
            prefs[KEY_DIET_TYPE] = profile.dietType.name
            prefs[KEY_RESTRICTIONS] = profile.restrictions.map { it.name }.toSet()
            prefs[KEY_FREEZER] = profile.preferFreezerFriendly
            prefs[KEY_EQUIPMENT] = profile.equipment.map { it.name }.toSet()
            prefs[KEY_ONBOARDED] = true
        }
    }

    private companion object {
        val KEY_DIET_TYPE = stringPreferencesKey("diet_type")
        val KEY_RESTRICTIONS = stringSetPreferencesKey("diet_restrictions")
        val KEY_FREEZER = booleanPreferencesKey("diet_freezer")
        val KEY_EQUIPMENT = stringSetPreferencesKey("diet_equipment")
        val KEY_ONBOARDED = booleanPreferencesKey("diet_onboarded")
    }
}
