package com.raywenderlich.videoplayerapp.adapter

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.raywenderlich.videoplayerapp.model.Short
import com.raywenderlich.videoplayerapp.ui.fragments.ShortVideoFragment

class ShortsAdapter(
    fragment: Fragment,
    private var shorts: List<Short>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return shorts.size
    }

    override fun createFragment(position: Int): Fragment {
        val short = shorts[position]
        return ShortVideoFragment.newInstance(short)
    }

    // this function is turning off the auto play shorts...
//    override fun getItemId(position: Int): Long {
//        return shorts[position].id.hashCode().toLong()
//    }

    override fun containsItem(itemId: Long): Boolean {
        return shorts.any { it.id.hashCode().toLong() == itemId }
    }

//    fun updateShorts(newShorts: List<Short>) {
//        shorts = newShorts
//        notifyDataSetChanged()
//    }

    fun updateShorts(newShorts: List<Short>) {
        val oldShorts = shorts
        shorts = newShorts

        // Calculate diff
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldShorts.size
            override fun getNewListSize(): Int = newShorts.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldShorts[oldItemPosition].id == newShorts[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldShorts[oldItemPosition] == newShorts[newItemPosition]
            }
        })

        // Apply diff
        diffResult.dispatchUpdatesTo(this)
    }
}