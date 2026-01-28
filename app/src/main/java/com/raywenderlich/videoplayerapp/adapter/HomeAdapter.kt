package com.raywenderlich.videoplayerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
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
import com.raywenderlich.videoplayerapp.databinding.ItemShortsGridBinding
import com.raywenderlich.videoplayerapp.databinding.ItemShortCellBinding
import com.raywenderlich.videoplayerapp.model.HomeItem
import com.raywenderlich.videoplayerapp.model.Video
import com.raywenderlich.videoplayerapp.model.Short
import com.raywenderlich.videoplayerapp.viewmodel.SubscriptionViewModel


class HomeAdapter(
    private var items: List<HomeItem>,
    private val onVideoClick: (Video) -> Unit,
    private val onShortClick: (Short) -> Unit,
    private val subscriptionViewModel: SubscriptionViewModel,
    private val showSubscribeOption: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_VIDEO = 0
        const val VIEW_TYPE_SHORTS_GRID = 1
    }

    private var currentlyPlayingHolder: VideoViewHolder? = null

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HomeItem.VideoItem -> VIEW_TYPE_VIDEO
            is HomeItem.ShortsGridItem -> VIEW_TYPE_SHORTS_GRID
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_VIDEO -> {
                val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                VideoViewHolder(binding)
            }
            VIEW_TYPE_SHORTS_GRID -> {
                val binding = ItemShortsGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ShortsGridViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VideoViewHolder -> {
                val item = items[position] as HomeItem.VideoItem
                holder.bind(item.video)
            }
            is ShortsGridViewHolder -> {
                val item = items[position] as HomeItem.ShortsGridItem
                holder.bind(item.shorts)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is VideoViewHolder) {
            if (currentlyPlayingHolder == holder) {
                currentlyPlayingHolder = null
            }
            holder.releasePlayer()
        }
    }

    inner class VideoViewHolder(private val binding: ItemVideoBinding): RecyclerView.ViewHolder(binding.root) {
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
    }

    inner class ShortsGridViewHolder(val binding: ItemShortsGridBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(shorts: List<Short>) {
            if(shorts.size > 0) bindShort(binding.shortCell1, shorts.getOrNull(0))
            if(shorts.size > 1) bindShort(binding.shortCell2, shorts.getOrNull(1))
            if(shorts.size > 2) bindShort(binding.shortCell3, shorts.getOrNull(2))
            if(shorts.size > 3) bindShort(binding.shortCell4, shorts.getOrNull(3))
        }

        private fun bindShort(cellBinding: ItemShortCellBinding, short: Short?) {
            if(short == null) {
                cellBinding.root.isVisible = false
                return
            }

            cellBinding.root.isVisible = true

            Glide.with(cellBinding.root.context)
                .load(short.thumbnailUrl)
                .placeholder(R.color.surface)
                .into(cellBinding.ivThumbnail)

            cellBinding.tvTitle.text = short.title
            cellBinding.tvViews.text = short.views

            cellBinding.root.setOnClickListener {
                onShortClick(short)
            }
        }
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
    fun updateItems(newItems: List<HomeItem>) {
        currentlyPlayingHolder?.pauseVideo()
        currentlyPlayingHolder = null

        items = newItems
        notifyDataSetChanged()
    }
}