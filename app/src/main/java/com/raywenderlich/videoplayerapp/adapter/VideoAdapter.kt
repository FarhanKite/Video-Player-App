package com.raywenderlich.videoplayerapp.adapter

import android.media.browse.MediaBrowser
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
    private val subscriptionViewModel: SubscriptionViewModel,
    private val showSubscribeOption: Boolean = true
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private var currentlyPlayingHolder: VideoViewHolder? = null

    inner class VideoViewHolder(private val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {

        var player: ExoPlayer? = null
        var currentVideo: Video? = null
        private var isMuted = true

        fun bind(video: Video) {
            currentVideo = video

            binding.tvTitle.text = video.title
            // \u2022 = •
            binding.tvDescription.text = "${video.channelName} \u2022 ${video.views} \u2022 ${video.uploadTime}"

            // Load thumbnail using Glide
            Glide.with(binding.root.context)
                .load(video.thumbnailUrl)
                .placeholder(R.color.surface)
                .into(binding.ivThumbnail)

            Glide.with(binding.root.context)
                .load(video.channelAvatar)
                .placeholder(R.color.surface)
                .into(binding.ivChannelAvatar)

            binding.ivThumbnail.isVisible = true
            binding.playerView.isVisible = false
            binding.btnMute.isVisible = false
            binding.progressBar.isVisible = false

            binding.videoContainer.setOnClickListener {
                onVideoClick(video)
            }

            binding.btnMute.setOnClickListener {
                toggleMute()
            }

            // Three dot menu click listener
            binding.ivMoreOptions.setOnClickListener {
                showPopupMenu(it, video)
            }
        }

        fun playVideo() {
            val video = currentVideo ?: return

            if (player == null) {
                player = ExoPlayer.Builder(binding.root.context).build().also { exoPlayer ->
                    binding.playerView.player = exoPlayer

                    val mediaItem = MediaItem.fromUri(video.videoUrl)
                    exoPlayer.setMediaItem(mediaItem)

                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true
                    exoPlayer.volume = 0f
                    exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

                    exoPlayer.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            when (playbackState) {
                                Player.STATE_BUFFERING -> {
                                    binding.progressBar.isVisible = true
                                }
                                Player.STATE_READY -> {
                                    binding.progressBar.isVisible = false
                                    binding.ivThumbnail.isVisible = false
                                    binding.playerView.isVisible = true
                                    binding.btnMute.isVisible = true
                                }
                                Player.STATE_ENDED -> {
                                    // will do later...
                                }
                            }
                        }
                    })
                }
            } else {
                player?.playWhenReady = true
                binding.ivThumbnail.isVisible = false
                binding.playerView.isVisible = true
                binding.btnMute.isVisible = true
            }

            updateMuteIcon()
        }

        fun pauseVideo() {
            player?.playWhenReady = false

            binding.ivThumbnail.isVisible = true
            binding.playerView.isVisible = false
            binding.btnMute.isVisible = false
            binding.progressBar.isVisible = false
        }

        fun releasePlayer() {
            player?.release()
            player = null

            binding.ivThumbnail.isVisible = true
            binding.playerView.isVisible = false
            binding.btnMute.isVisible = false
            binding.progressBar.isVisible = false
        }

        private fun toggleMute() {
            isMuted = !isMuted
            player?.volume = if (isMuted) 0f else 1f
            updateMuteIcon()
        }

        private fun updateMuteIcon() {
            val iconRes = if (isMuted) {
                R.drawable.ic_volume_off
            } else {
                R.drawable.ic_volume_on
            }
            binding.btnMute.setImageResource(iconRes)
        }

        private fun showPopupMenu(view: View, video: Video) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.video_options_menu, popup.menu)

            if(!showSubscribeOption) {
                popup.menu.findItem(R.id.menu_subscribe)?.isVisible = false
            } else {
                val subscribeMenuItem = popup.menu.findItem(R.id.menu_subscribe)
                val isSubscribed = subscriptionViewModel.isSubscribed(video.channelName)
                subscribeMenuItem.title = if(isSubscribed) "Unsubscribe" else "Subscribe"
            }

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
            subscriptionViewModel.subscribeToChannel(channelName, video.channelAvatar)
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

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)

        // Release player when view is recycled
        if (currentlyPlayingHolder == holder) {
            currentlyPlayingHolder = null
        }
        holder.releasePlayer()
    }

    fun onViewHolderVisible(holder: VideoViewHolder) {
        // Pause currently playing
        currentlyPlayingHolder?.pauseVideo()

        // Play new
        holder.playVideo()
        currentlyPlayingHolder = holder
    }

    fun pauseAllVideos() {
        currentlyPlayingHolder?.pauseVideo()
        currentlyPlayingHolder = null
    }

    // Update video list
    fun updateVideos(newVideos: List<Video>) {
        currentlyPlayingHolder?.pauseVideo()
        currentlyPlayingHolder = null

        videos = newVideos
        notifyDataSetChanged()
    }
}