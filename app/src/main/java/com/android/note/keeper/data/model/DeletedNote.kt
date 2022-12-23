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
@Entity(tableName = Constants.DELETED_TABLE_NAME)
data class DeletedNote(
    @PrimaryKey
    val _id: Int,  //will remain as same for restore purpose
    val title: String,
    val content: String,
    val color: String = Constants.COLOR_DEFAULT, //revert back to default
    val tag: Int = Constants.TAG_NOTE,
    val pin:Boolean = false,  //revert back to default
    val markAsComplete : Boolean = false,  //revert back to default
    val archived : Boolean = false, //revert back to default
    val isPasswordProtected: Boolean = false,
    val created: Long = System.currentTimeMillis()
) : Parcelable {
    val formattedDate: String
        get() = DateFormat.getDateInstance().format(created)
}
