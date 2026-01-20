package com.raywenderlich.videoplayerapp.adapter

import androidx.fragment.app.Fragment
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

    fun updateShorts(newShorts: List<Short>) {
        shorts = newShorts
        notifyDataSetChanged()
    }
}