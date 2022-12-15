package com.android.note.keeper.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.note.keeper.util.Constants
import kotlinx.parcelize.Parcelize
import java.text.DateFormat


@Keep
@Parcelize
@Entity(tableName = Constants.TABLE_NAME)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,
    val title: String,
    val content: String,
    val color: String = Constants.COLOR_DEFAULT, //default color is colorSurface
    val tag: Int = Constants.TAG_NOTE, // 1 -> note ; 2 -> checkList
    val pin:Boolean = false,  // pin the note or not
    val markAsComplete : Boolean = false,  // mark as completed to show at bottom
    val isPasswordProtected: Boolean = false,
    val created: Long = System.currentTimeMillis()
) : Parcelable {
    val formattedDate: String
        get() = DateFormat.getDateInstance().format(created)
}
