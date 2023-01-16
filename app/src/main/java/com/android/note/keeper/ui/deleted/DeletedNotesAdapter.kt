package com.android.note.keeper.ui.deleted

import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.note.keeper.data.model.DeletedNote
import com.android.note.keeper.databinding.DeletedNoteListItemBinding
import com.android.note.keeper.util.Utils
import kotlin.math.roundToInt

class DeletedNotesAdapter(
    private val listener: OnItemClickListener
) : ListAdapter<DeletedNote, DeletedNotesAdapter.NoteViewHolder>(DiffCallback()) {

    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding =
            DeletedNoteListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentItem = getItem(position)
        //holder.bind(currentItem)

        tracker?.let {
            holder.bind(currentItem, it.isSelected(position.toLong()))
        }
    }

    inner class NoteViewHolder(private val binding: DeletedNoteListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val Int.toPx
            get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).roundToInt()

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }

              /*  root.setOnLongClickListener {

                    //todo
                    true
                }*/

            }
        }

        fun bind(note: DeletedNote, isActivated: Boolean = false) {
            binding.apply {
                parentCard.isActivated = isActivated
                txtTitle.isVisible = note.title.isNotBlank()
                txtSubtitle.isVisible = note.content.isNotBlank()
                txtTitle.text = note.title
                txtSubtitle.text = note.content
                if (parentCard.isActivated) parentCard.strokeWidth = 2.toPx
                else 1.toPx
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
            }
    }

    interface OnItemClickListener {
        fun onItemClick(task: DeletedNote)
        fun onItemLongClick(task: DeletedNote)
    }

    class DiffCallback : DiffUtil.ItemCallback<DeletedNote>() {
        override fun areItemsTheSame(oldItem: DeletedNote, newItem: DeletedNote) =
            oldItem._id == newItem._id

        override fun areContentsTheSame(oldItem: DeletedNote, newItem: DeletedNote) =
            oldItem == newItem
    }


}

