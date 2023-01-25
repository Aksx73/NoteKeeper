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
    val created: Long = System.currentTimeMillis()
) : Parcelable {
    val formattedDate: String
        get() = DateFormat.getDateInstance().format(created)
}
