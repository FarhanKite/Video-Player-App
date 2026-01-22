package com.raywenderlich.videoplayerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.raywenderlich.videoplayerapp.databinding.ItemSubscriptionBinding
import com.raywenderlich.videoplayerapp.model.Channel

class SubscriptionAdapter(
    private var channels: List<Channel>,
    private val onUnsubscribeClick: (Channel) -> Unit
) : RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder>() {

    inner class SubscriptionViewHolder(private val binding: ItemSubscriptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(channel: Channel) {
            binding.tvChannelName.text = channel.name

            binding.btnUnsubscribe.setOnClickListener {
                onUnsubscribeClick(channel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val binding = ItemSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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