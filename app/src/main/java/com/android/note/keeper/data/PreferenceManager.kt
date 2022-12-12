package com.android.note.keeper.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.android.note.keeper.di.ApplicationScope
import com.android.note.keeper.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private const val TAG = "PreferencesManager"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(Constants.PREFERENCE_NAME)

class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    val masterPasswordFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[MASTER_PASSWORD] ?: DEFAULT_PASSWORD
        }
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emptyPreferences()
            } else {
                throw exception
            }
        }

    suspend fun setMasterPassword(pass: String) {
        dataStore.edit { preferences ->
            preferences[MASTER_PASSWORD] = pass
        }
    }

    companion object {
        val MASTER_PASSWORD = stringPreferencesKey("master_password")
        const val DEFAULT_PASSWORD = ""
    }
}