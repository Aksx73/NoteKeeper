package com.android.note.keeper.util

import android.app.Activity
import android.content.Context

object Constants {

    const val FRAGMENT_RESULT_REQUEST_KEY = "fragment_result_key"
    const val NOTE_DELETE_RESULT_OK = Activity.RESULT_FIRST_USER
    const val NOTE_ADDED_RESULT_OK = Activity.RESULT_FIRST_USER + 1
    const val NOTE_UPDATED_RESULT_OK = Activity.RESULT_FIRST_USER + 2
    const val NOTE_ARCHIVED_RESULT_OK = Activity.RESULT_FIRST_USER + 3

    const val TAG_NOTE = 1
    const val TAG_CHECKLIST = 2
    const val DATABASE_NAME = "NotesDatabase"
    const val DATABASE_DELETED_NAME = "DeletedNotesDatabase"
    const val TABLE_NAME = "notes_table"
    const val DELETED_TABLE_NAME = "deleted_notes_table"
    const val PREFERENCE_NAME = "user_preferences"

    const val COLOR_DEFAULT = "DEFAULT"
    const val COLOR_RED = "RED"
    const val COLOR_YELLOW = "YELLOW"
    const val COLOR_ORANGE = "ORANGE"
    const val COLOR_GREEN = "GREEN"
    const val COLOR_BLUE = "BLUE"
    const val COLOR_TEAL = "TEAL"
    const val COLOR_GRAY = "GRAY"
    const val COLOR_PURPLE = "PURPLE"


}