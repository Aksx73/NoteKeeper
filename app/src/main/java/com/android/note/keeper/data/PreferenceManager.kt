package com.android.note.keeper.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.android.note.keeper.di.ApplicationScope
import com.android.note.keeper.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
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

   /* suspend fun getViewMode(): Int {
        val preferences = dataStore.data.first()
        return preferences[VIEW_MODE] ?: SINGLE_COLUMN
    }*/

    val dynamicThemingFlow : Flow<Boolean> = dataStore.data
        .map { preference ->
            preference[DYNAMIC_COLORS] ?: DEFAULT_DYNAMIC_COLOR
        }
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emptyPreferences()
            } else {
                throw exception
            }
        }

    val viewModeFlow: Flow<Int> = dataStore.data
        .map { preference ->
            preference[VIEW_MODE] ?: SINGLE_COLUMN
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

    suspend fun setViewMode(mode:Int){
        dataStore.edit { preference ->
            preference[VIEW_MODE] = mode
        }
    }

    suspend fun setDynamicTheming(isEnabled:Boolean){
        dataStore.edit { preference ->
            preference[DYNAMIC_COLORS] = isEnabled
        }
    }

    companion object {
        val MASTER_PASSWORD = stringPreferencesKey("master_password")
        val VIEW_MODE = intPreferencesKey("view_mode")
        val DARK_THEME = intPreferencesKey("dark_theme")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_theming")
        const val DEFAULT_PASSWORD = ""
        const val SINGLE_COLUMN = 0
        const val MULTI_COLUMN = 1
        const val DEFAULT_DYNAMIC_COLOR = true
    }
}