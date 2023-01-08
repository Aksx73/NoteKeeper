package com.android.note.keeper.ui.deleted

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.note.keeper.data.model.DeletedNote
import com.android.note.keeper.databinding.DeletedNoteListItemBinding
import com.android.note.keeper.util.ColorsUtil
import com.android.note.keeper.util.Constants
import com.android.note.keeper.util.Utils

class DeletedNotesAdapter (
    private val listener: OnItemClickListener
) : ListAdapter<DeletedNote, DeletedNotesAdapter.NoteViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding =
            DeletedNoteListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class NoteViewHolder(private val binding: DeletedNoteListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }
            }
        }

        fun bind(note: DeletedNote) {
            binding.apply {
                txtTitle.isVisible = !note.title.isNullOrBlank()
                txtTitle.text = note.title
                txtSubtitle.text = note.content
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(task: DeletedNote)
    }

    class DiffCallback : DiffUtil.ItemCallback<DeletedNote>() {
        override fun areItemsTheSame(oldItem: DeletedNote, newItem: DeletedNote) =
            oldItem._id == newItem._id

        override fun areContentsTheSame(oldItem: DeletedNote, newItem: DeletedNote) =
            oldItem == newItem
    }
}