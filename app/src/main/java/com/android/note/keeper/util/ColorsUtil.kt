package com.android.note.keeper.util

import com.android.note.keeper.R

class ColorsUtil {

    private val colorsList = arrayOf(
        Constants.COLOR_DEFAULT,// 0
        Constants.COLOR_RED,// 1
        Constants.COLOR_ORANGE,// 2
        Constants.COLOR_YELLOW,// 3
        Constants.COLOR_GREEN,// 4
        Constants.COLOR_BLUE,// 5
        Constants.COLOR_TEAL,// 6
        Constants.COLOR_PURPLE,// 7
        Constants.COLOR_GRAY,// 8
    )

    fun getPosition(position: Int?): String {
        if (position == null || position > getSize())
            return colorsList[0]
        return colorsList[position]

    }

    fun getPositionFromName(colorName:String):Int{
        return colorsList.indexOf(colorName)
    }

    fun getSize(): Int {
        return colorsList.size
    }

    fun getColor(name:String):Int{
        return when(name){
            Constants.COLOR_RED -> R.color.note_color_red
            Constants.COLOR_YELLOW -> R.color.note_color_yellow
            Constants.COLOR_ORANGE -> R.color.note_color_orange
            Constants.COLOR_GREEN -> R.color.note_color_green
            Constants.COLOR_GRAY -> R.color.note_color_gray
            Constants.COLOR_PURPLE -> R.color.note_color_purple
            Constants.COLOR_BLUE -> R.color.note_color_blue
            Constants.COLOR_TEAL -> R.color.note_color_teal
            else -> 0
        }
    }
}