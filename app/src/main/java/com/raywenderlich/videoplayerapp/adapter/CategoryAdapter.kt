package com.raywenderlich.videoplayerapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.videoplayerapp.R

class CategoryAdapter(
    private val categories: List<String>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    inner class CategoryViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(category: String, position: Int) {
            textView.text = category

            // Update background based on selection
            val backgroundRes = if (position == selectedPosition) {
                R.drawable.bg_category_selected
            } else {
                R.drawable.bg_category_unselected
            }
            textView.setBackgroundResource(backgroundRes)

            // Update text color based on selection
            val textColor = if (position == selectedPosition) {
                ContextCompat.getColor(textView.context, R.color.black)
            } else {
                ContextCompat.getColor(textView.context, R.color.text_primary)
            }
            textView.setTextColor(textColor)

            textView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition

                // Notify items to update UI
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                onCategoryClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false) as TextView
        return CategoryViewHolder(textView)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position)
    }

    override fun getItemCount() = categories.size
}