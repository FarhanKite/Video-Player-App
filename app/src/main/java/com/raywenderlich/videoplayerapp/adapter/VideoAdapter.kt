package com.raywenderlich.videoplayerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.raywenderlich.videoplayerapp.R
import com.raywenderlich.videoplayerapp.model.Video

class VideoAdapter(
    private var videos: List<Video>,
    private val onVideoClick: (Video) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivThumbnail: ImageView = view.findViewById(R.id.iv_thumbnail)
        private val ivChannelAvatar: ImageView = view.findViewById(R.id.iv_channel_avatar)
        private val tvTitle: TextView = view.findViewById(R.id.tv_title)
        private val tvDescription: TextView = view.findViewById(R.id.tv_description)
        private val ivMoreOptions: ImageView = view.findViewById(R.id.iv_more_options)

        fun bind(video: Video) {
            tvTitle.text = video.title
            tvDescription.text = "${video.channelName} • ${video.views} • ${video.uploadTime}"

            // Load thumbnail using Glide
            Glide.with(itemView.context)
                .load(video.thumbnailUrl)
                .placeholder(R.color.surface)
                .into(ivThumbnail)

            // Video click listener
            itemView.setOnClickListener {
                onVideoClick(video)
            }

            // Thumbnail click listener
            ivThumbnail.setOnClickListener {
                onVideoClick(video)
            }

            // Three dot menu click listener
            ivMoreOptions.setOnClickListener {
                showPopupMenu(it, video)
            }
        }

        private fun showPopupMenu(view: View, video: Video) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.video_options_menu, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_share -> {
                        Toast.makeText(view.context, "Share: ${video.title}", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_not_interested -> {
                        Toast.makeText(view.context, "Not interested", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_report -> {
                        Toast.makeText(view.context, "Report: ${video.title}", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
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