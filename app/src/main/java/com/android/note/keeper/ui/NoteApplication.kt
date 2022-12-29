package com.android.note.keeper.ui

import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoteApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // todo use value from datastore to enable dark mode & dynamic theming
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}