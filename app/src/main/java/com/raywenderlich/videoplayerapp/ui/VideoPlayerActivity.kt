package com.raywenderlich.videoplayerapp.ui

import kotlin.jvm.java

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import com.raywenderlich.videoplayerapp.databinding.ActivityVideoPlayerBinding
import com.raywenderlich.videoplayerapp.model.Video

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null
    private var video: Video? = null
    private var playWhenReady = true
    private var currentPosition = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get video from intent
        video = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("VIDEO", Video::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("VIDEO")
        }

        if (video == null) {
            finish()
            return
        }
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer() {

        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer

            // Set audio attributes for proper audio playback
            val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .build()

            exoPlayer.setAudioAttributes(audioAttributes, true)

            video?.let {
                val mediaItem = MediaItem.fromUri(it.videoUrl)
                exoPlayer.setMediaItem(mediaItem)
            }

            exoPlayer.playWhenReady = playWhenReady
            exoPlayer.seekTo(currentPosition)
            exoPlayer.prepare()

            // Add listener for player events
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> {
                            // Show loading indicator if needed
                        }
                        Player.STATE_READY -> {
                            // Hide loading indicator if needed
                        }
                        Player.STATE_ENDED -> {
                            // Video ended
                            finish()
                        }
                    }
                }
            })
        }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playWhenReady = exoPlayer.playWhenReady
            currentPosition = exoPlayer.currentPosition
            exoPlayer.release()
        }
        player = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        player?.let {
            outState.putLong("currentPosition", it.currentPosition)
            outState.putBoolean("playWhenReady", it.playWhenReady)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentPosition = savedInstanceState.getLong("currentPosition", 0L)
        playWhenReady = savedInstanceState.getBoolean("playWhenReady", true)
    }
}