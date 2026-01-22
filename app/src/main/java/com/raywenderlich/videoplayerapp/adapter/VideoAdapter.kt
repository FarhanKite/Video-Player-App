package com.raywenderlich.videoplayerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.raywenderlich.videoplayerapp.R
import com.raywenderlich.videoplayerapp.databinding.ItemVideoBinding
import com.raywenderlich.videoplayerapp.model.Video
import com.raywenderlich.videoplayerapp.ui.fragments.SubscriptionFragment
import com.raywenderlich.videoplayerapp.viewmodel.SubscriptionViewModel

class VideoAdapter(
    private var videos: List<Video>,
    private val onVideoClick: (Video) -> Unit,
    private val subscriptionViewModel: SubscriptionViewModel
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(private val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(video: Video) {
            binding.tvTitle.text = video.title
            // \u2022 = •
            binding.tvDescription.text = "${video.channelName} \u2022 ${video.views} \u2022 ${video.uploadTime}"

            // Load thumbnail using Glide
            Glide.with(binding.root.context)
                .load(video.thumbnailUrl)
                .placeholder(R.color.surface)
                .into(binding.ivThumbnail)

            // Video click listener
            binding.root.setOnClickListener {
                onVideoClick(video)
            }

            // Thumbnail click listener
            binding.ivThumbnail.setOnClickListener {
                onVideoClick(video)
            }

            // Three dot menu click listener
            binding.ivMoreOptions.setOnClickListener {
                showPopupMenu(it, video)
            }
        }

        private fun showPopupMenu(view: View, video: Video) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.video_options_menu, popup.menu)

            val subscribeMenuItem = popup.menu.findItem(R.id.menu_subscribe)
            val isSubscribed = subscriptionViewModel.isSubscribed(video.channelName)
            subscribeMenuItem.title = if(isSubscribed) "Unsubscribe" else "Subscribe"

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_subscribe -> {
                        handleSubscription(view, video)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }

    private fun handleSubscription(view: View, video: Video) {
        val channelName = video.channelName

        if(subscriptionViewModel.isSubscribed(channelName)) {
            subscriptionViewModel.unsubscribeFromChannel(channelName)
            Toast.makeText(view.context, "Unsubscribed from $channelName", Toast.LENGTH_SHORT).show()
        } else {
            subscriptionViewModel.subscribeToChannel(channelName)
            Toast.makeText(view.context, "Subscribed to $channelName", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    override fun getItemCount() = videos.size

    // Update video list
    fun updateVideos(newVideos: List<Video>) {
        videos = newVideos
        notifyDataSetChanged()
    }
}