package com.android.note.keeper.ui

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.android.note.keeper.data.PreferenceManager
import com.android.note.keeper.util.Utils
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class NoteApplication : Application() {

    @Inject
    lateinit var dataStoreManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()

        // todo use value from datastore to enable dark mode & dynamic theming
        if (runBlocking { dataStoreManager.dynamicThemingFlow.first() }) {
            //Utils.applyDynamicColors(this)
            //DynamicColors.applyToActivitiesIfAvailable(this)
        } else {
            //todo
        }

        when (runBlocking { dataStoreManager.themeMode.first() }) {
            AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
            AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}