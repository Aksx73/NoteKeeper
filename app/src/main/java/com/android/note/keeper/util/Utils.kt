package com.android.note.keeper.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.text.InputType
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.internal.ContextUtils
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
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

    fun getFormattedTime(timeMilli: Long): String { // 12:45 am
        val date = Date(timeMilli)
        val timeZoneDate = SimpleDateFormat("HH:mm a", Locale.getDefault())
        return timeZoneDate.format(date)
    }

    fun getFormattedDate(timeMilli: Long): String { // 14 Dec
        val date = Date(timeMilli)
        val timeZoneDate = SimpleDateFormat("dd MMM", Locale.getDefault())
        return timeZoneDate.format(date)
    }

    fun Context.isDarkThemeOn(): Boolean {
        return resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }

    fun Context.dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    fun Context.pxToDp(px: Int): Int {
        return (px / resources.displayMetrics.density).toInt()
    }

    @ColorInt
    fun getColorFromAttr(
        context: Context,
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        context.theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    fun setColorAlpha(percentage: Int, colorCode: String): String {
        val decValue = percentage.toDouble() / 100 * 255
        val rawHexColor = colorCode.replace("#", "")
        val str = StringBuilder(rawHexColor)
        if (Integer.toHexString(decValue.toInt()).length == 1)
            str.insert(0, "#0" + Integer.toHexString(decValue.toInt()))
        else
            str.insert(0, "#" + Integer.toHexString(decValue.toInt()))
        return str.toString()
    }

    fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = snapMode
            override fun getHorizontalSnapPreference(): Int = snapMode
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }

    @SuppressLint("RestrictedApi")
    private fun recreateActivityIfPossible(context: Context) {
        val activity = ContextUtils.getActivity(context)
        activity?.recreate()
    }


    fun setTooltip(
        context: Context,
        message: String,
        @DrawableRes icon: Int,
        @ColorRes colorText: Int,
        @ColorRes backgroundColor: Int,
        lifecycle: LifecycleOwner
    ): Balloon {
        return Balloon.Builder(context)
            .setWidthRatio(0f)
            .setHeight(BalloonSizeSpec.WRAP)
            .setText(message)
            .setTextColorResource(colorText)
            .setTextSize(14f)
           // .setIconDrawableResource(icon)
            .setIconSize(18)
            .setIconColorResource(colorText)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowSize(8)
            .setArrowPosition(0.5f)
            .setPadding(12)
            .setCornerRadius(6f)
            .setBackgroundColorResource(backgroundColor)
            .setBalloonAnimation(BalloonAnimation.FADE)
            .setLifecycleOwner(lifecycle)
            .setAutoDismissDuration(3000L)
            .build()

    }
}