package com.android.note.keeper.util

import com.android.note.keeper.R

class ColorsUtil {

    private val colorsList = arrayOf(
        R.color.note_color_red,// 0
        R.color.note_color_orange,// 1
        R.color.note_color_yellow,// 2
        R.color.note_color_green,// 3
        R.color.note_color_blue,// 4
        R.color.note_color_teal,// 5
        R.color.note_color_purple,// 6
        R.color.note_color_gray,// 7
    )

    fun getColor(position: Int?): Int {
        if (position == null || position > getSize())
            return colorsList[0]
        return colorsList[position]
    }

    fun getSize(): Int {
        return colorsList.size
    }
}