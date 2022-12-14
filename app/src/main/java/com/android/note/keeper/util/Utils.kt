package com.android.note.keeper.util

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.text.InputType
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*


object Utils {

    /**
     * hide soft keyboard
     * */
    fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }

    /**
     * show soft keyboard
     * */
    fun showKeyboard(activity: Activity, editText: TextInputEditText) {
        val inputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * make edit text non editable
     * */
    fun disableInput(editText: EditText) {
        editText.inputType = InputType.TYPE_NULL
        //editText.setTextIsSelectable(false)
        editText.setOnKeyListener { _, _, _ ->
            true // Blocks input from hardware keyboards.
        }
    }

    fun showSnackBar(view: View, message: String, anchor: View) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .setAnchorView(anchor)
            .show()
    }

    fun getFormattedTime(timeMilli:Long): String{ // 12:45 am
        var date = Date(timeMilli)
        val timeZoneDate = SimpleDateFormat("HH:mm a", Locale.getDefault())
        return timeZoneDate.format(date)
    }

    fun getFormattedDate(timeMilli:Long): String{ // 14 Dec
        var date = Date(timeMilli)
        val timeZoneDate = SimpleDateFormat("dd MMM", Locale.getDefault())
        return timeZoneDate.format(date)
    }


}