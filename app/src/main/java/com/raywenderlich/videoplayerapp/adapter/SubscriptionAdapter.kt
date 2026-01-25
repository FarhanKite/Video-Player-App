package com.raywenderlich.videoplayerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
//import androidx.core.R
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.raywenderlich.videoplayerapp.databinding.ItemSubscriptionBinding
import com.raywenderlich.videoplayerapp.model.Channel
import com.raywenderlich.videoplayerapp.R

class SubscriptionAdapter(
    private var channels: List<Channel>,
    private val onUnsubscribeClick: (Channel) -> Unit
) : RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder>() {

    inner class SubscriptionViewHolder(private val binding: ItemSubscriptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(channel: Channel) {
            binding.tvChannelName.text = channel.name

            Glide.with(binding.root.context)
                .load(channel.avatarUrl)
                .placeholder(R.drawable.ic_subscription)
                .error(R.drawable.ic_subscription)
                .circleCrop()
                .into(binding.ivChannelIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val binding = ItemSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // val binding = LayoutInflater.from(parent.context).inflate(R.layout.item_subscription, parent, false) as ItemSubscriptionBinding
        return SubscriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        holder.bind(channels[position])
    }

    override fun getItemCount(): Int {
        return channels.size
    }

    fun updateChannels(newChannels: List<Channel>) {
        channels = newChannels
        notifyDataSetChanged()
    }
}