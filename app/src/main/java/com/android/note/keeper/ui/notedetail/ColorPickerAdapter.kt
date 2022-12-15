package com.android.note.keeper.ui.notedetail

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android.note.keeper.R
import com.android.note.keeper.util.ColorsUtil
import com.android.note.keeper.util.Utils
import com.google.android.material.card.MaterialCardView


class ColorPickerAdapter(val context: Context, private val viewModel: NoteDetailViewModel) :
    RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>() {
    private var selectedColor = 0
    private val colorsUtil = ColorsUtil()

    init {
        //selectedColor = viewModel.getSelectedColor()

        viewModel.currentNote.value?.let {
            selectedColor = colorsUtil.getPositionFromName(it.color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.color_circle_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        //handle selected tick icon
        if (position == selectedColor) {
            holder.imgChecked.isVisible = true
            holder.colorCard.strokeWidth = 6
            holder.colorCard.strokeColor = Utils.getColorFromAttr(
                context,
                com.google.android.material.R.attr.colorPrimary
            )
        } else {
            holder.imgChecked.isVisible = false
            holder.colorCard.strokeWidth = 3
            holder.colorCard.strokeColor = Utils.getColorFromAttr(
                context,
                com.google.android.material.R.attr.colorOutline
            )
        }

        if (position == 0) {
            val colorInt =
                Utils.getColorFromAttr(context, com.google.android.material.R.attr.colorSurface)
            holder.colorCard.setCardBackgroundColor(colorInt)
        } else {
            val colorName = colorsUtil.getPosition(position)
            val colorHex = context.resources.getString(colorsUtil.getColor(colorName))
            val colorInt = Color.parseColor(colorHex)
            holder.colorCard.setCardBackgroundColor(colorInt)
        }

        holder.colorCard.setOnClickListener {
            selectedColor = position
            viewModel.setSelectedColor(position)
            notifyDataSetChanged()
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorCard: MaterialCardView = itemView.findViewById(R.id.color_card)
        val imgChecked: ImageView = itemView.findViewById(R.id.checked)
    }

    override fun getItemCount(): Int {
        return colorsUtil.getSize()
    }
}