package com.android.note.keeper.ui

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoteApplication : Application() {

   /* override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: NoteApplication
            private set
    }*/
}