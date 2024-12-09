package com.mobile_programming.sari_sari_inventory_app.data

import android.content.Context
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val USER_PREFERENCES_NAME = "user_preferences"
val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

interface UserPreferencesDataStoreApi {
    suspend fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T>
    suspend fun <T> putPreference(key: Preferences.Key<T>, value: T)
    suspend fun <T> removePreference(key: Preferences.Key<T>)
    suspend fun <T> clearAllPreference()
}

class UserPreferencesDataStoreHelper(
    context: Context
) : UserPreferencesDataStoreApi {

    private val dataSource = context.dataStore

    override suspend fun <T> getPreference(
        key: Preferences.Key<T>,
        defaultValue: T
    ): Flow<T> = dataSource.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        val result = preferences[key] ?: defaultValue
        result
    }

    override suspend fun <T> putPreference(
        key: Preferences.Key<T>,
        value: T
    ) {
        dataSource.edit { preferences ->
            preferences[key] = value
        }
    }

    override suspend fun <T> removePreference(key: Preferences.Key<T>) {
        dataSource.edit { preferences ->
            preferences.remove(key)
        }
    }

    override suspend fun <T> clearAllPreference() {
        dataSource.edit { preferences ->
            preferences.clear()
        }
    }

}