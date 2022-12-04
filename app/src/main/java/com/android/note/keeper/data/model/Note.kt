package com.android.note.keeper.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.note.keeper.util.Constants
import java.text.DateFormat
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Constants.TABLE_NAME)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,
    val title: String,
    val content: String,
    val tag: Int = Constants.TAG_NOTE, // 1 -> note ; 2 -> checkList
    val isPasswordProtected: Boolean = false,
    val password: String? = null,
    val created: Long = System.currentTimeMillis()
) : Parcelable {
    val formattedDate: String
        get() = DateFormat.getDateInstance().format(created)
}
