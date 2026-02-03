package com.raywenderlich.videoplayerapp.utils

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlin.math.exp

object PlayerManager {
    private var exoPlayer: ExoPlayer? = null
    private var currentPlayerView: PlayerView? = null
    private var currentVideoUrl: String? = null

    private var isPlayerReady = false
    private var shouldAutoPlay = false
    private var lastVolume = 0.3f

    private var onPlayerReadyListener: (() -> Unit)? = null
    private var onBufferingListener: ((Boolean) -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null

    private fun initializePlayer(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = lastVolume
                playWhenReady = false

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                isPlayerReady = false
                                onBufferingListener?.invoke(true)
                            }

                            Player.STATE_READY -> {
                                isPlayerReady = true
                                onBufferingListener?.invoke(false)

                                if (shouldAutoPlay) {
                                    exoPlayer?.play()
                                    shouldAutoPlay = false
                                }
                                onPlayerReadyListener?.invoke()
                            }

                            Player.STATE_ENDED -> {
                                Log.d("PlayerManager", "STATE_ENDED")
                            }

                            Player.STATE_IDLE -> {
                                Log.d("PlayerManager", "STATE_IDLE")
                            }
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        isPlayerReady = false
                        onBufferingListener?.invoke(false)
                        onErrorListener?.invoke(error.message ?: "Unknown error")
                        Log.e("PlayerManager", "Player error: ${error.message}")
                    }
                })
            }
            Log.d("PlayerManager", "ExoPlayer initialized")
        }
    }

    fun attachPlayer(
        playerView: PlayerView,
        videoUrl: String,
        autoPlay: Boolean = true,
        context: Context
    ) {
        Log.d("PlayerManager", "attachPlayer called - URL: $videoUrl, autoPlay: $autoPlay")
        initializePlayer(context)

        if (currentPlayerView != null && currentPlayerView != playerView) {
            currentPlayerView?.player = null
            Log.d("PlayerManager", "Detached from previous PlayerView")
        }

        currentPlayerView = playerView
        playerView.player = exoPlayer

        if (currentVideoUrl != videoUrl) {
            currentVideoUrl = videoUrl
            isPlayerReady = false
            shouldAutoPlay = autoPlay

            exoPlayer?.let { player ->
                val mediaItem = MediaItem.fromUri(videoUrl)
                player.setMediaItem(mediaItem)
                player.prepare()
                Log.d("PlayerManager", "Preparing new video: $videoUrl")
            }
        } else {
            if (autoPlay && isPlayerReady) {
                exoPlayer?.play()
                Log.d("PlayerManager", "Resuming same video")
            }
        }
    }

    fun detachPlayer() {
        Log.d("PlayerManager", "detachPlayer called")
        exoPlayer?.pause()
        currentPlayerView?.player = null
        currentPlayerView = null
    }

    fun play() {
        exoPlayer?.let { player ->
            if (isPlayerReady) {
                player.play()
                Log.d("PlayerManager", "Playing video")
            } else {
                shouldAutoPlay = true
                Log.d("PlayerManager", "Scheduled to play when ready")
            }
        }
    }

    fun pause() {
        exoPlayer?.pause()
        shouldAutoPlay = false
        Log.d("PlayerManager", "Video paused")
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: false
    }

    fun setVolume(volume: Float) {
        lastVolume = volume.coerceIn(0f, 1f)
        exoPlayer?.volume = lastVolume
    }

    fun getVolume(): Float {
        return exoPlayer?.volume ?: lastVolume
    }

    fun setPlayerCallbacks(
        onReady: (() -> Unit)? = null,
        onBuffering: ((Boolean) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        onPlayerReadyListener = onReady
        onBufferingListener = onBuffering
        onErrorListener = onError
    }

    fun clearCallbacks() {
        onPlayerReadyListener = null
        onBufferingListener = null
        onErrorListener = null
    }

    fun releasePlayer() {
        Log.d("PlayerManager", "Releasing ExoPlayer")
        exoPlayer?.let { player ->
            try {
                player.stop()
                player.release()
            } catch (e: Exception) {
                Log.e("PlayerManager", "Error releasing player: ${e.message}", e)
            }
        }

        exoPlayer = null
        currentPlayerView?.player = null
        currentPlayerView = null
        currentVideoUrl = null
        isPlayerReady = false
        shouldAutoPlay = false

        clearCallbacks()
    }
}