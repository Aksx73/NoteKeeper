package com.android.note.keeper.util

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.android.note.keeper.ui.deleted.DeletedNotesAdapter

class MyItemDetailsLookup (private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as DeletedNotesAdapter.NoteViewHolder)
                .getItemDetails()
        }
        return null
    }
}