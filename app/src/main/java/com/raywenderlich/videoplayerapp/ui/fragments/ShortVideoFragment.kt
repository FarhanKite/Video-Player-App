package com.raywenderlich.videoplayerapp.ui.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.raywenderlich.videoplayerapp.R
import com.raywenderlich.videoplayerapp.databinding.FragmentShortVideoBinding
import com.raywenderlich.videoplayerapp.model.Short
import kotlinx.coroutines.delay
import kotlin.jvm.java
import kotlin.math.exp

class ShortVideoFragment : Fragment() {
    private var _binding: FragmentShortVideoBinding? = null
    private val binding get() = _binding!!

    private var short: Short? = null

    private var player: ExoPlayer? = null
    private var isPlaying = false
    private var isMuted = false
    private var isPlayerReady = false
    private var shouldAutoPlay = false

    private var lastVolume = 0.3f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShortVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        short = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("short", Short::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("short")
        }

        short?.let { shortData ->
            setupUI(shortData)
            initializePlayer(shortData.videoUrl)
            setupClickListeners(shortData)
        }

        Log.d(
            "${this::class.java.simpleName}",
            "${Throwable().stackTrace[0].methodName} ${short?.id}"
        )
    }

    private fun setupUI(short: Short) {
        binding.playerView.useController = true
        binding.playerView.controllerShowTimeoutMs = 1000

        binding.tvTitle.text = short.title
        binding.tvChannelName.text = short.channelName
        binding.tvLike.text = short.likes
        binding.tvViews.text = short.views

        binding.progressBar.isVisible = true

//        binding.root.postDelayed({
//            binding.progressBar.isVisible = false
//        }, 500)

//        binding.root.post({
//            binding.progressBar.isVisible = false
//        })
    }

    private fun initializePlayer(videoUrl: String) {
        releasePlayer()

        binding.progressBar.isVisible = true
        isPlayerReady = false

        try {
            player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
                binding.playerView.player = exoPlayer

                val mediaItem = MediaItem.fromUri(videoUrl)
                exoPlayer.setMediaItem(mediaItem)

                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

                exoPlayer.volume = lastVolume

                exoPlayer.playWhenReady = false

                exoPlayer.prepare()

                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                binding.progressBar.isVisible = true
                            }

                            Player.STATE_READY -> {
                                binding.progressBar.isVisible = false
                                isPlayerReady = true

                                if (shouldAutoPlay) {
                                    exoPlayer.play()
                                    shouldAutoPlay = false
                                }
                            }

                            Player.STATE_ENDED -> {

                            }
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)

                        binding.progressBar.isVisible = false

                        if (isAdded) {
                            Toast.makeText(
                                requireContext(),
                                "Error playing video: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        binding.root.postDelayed({
                            if (isAdded && _binding != null) {
                                short?.videoUrl?.let { url ->
                                    initializePlayer(url)
                                }
                            }
                        }, 1000)
                    }
                })
            }
            updateVolumeIcon()

        } catch (e: Exception) {
            binding.progressBar.isVisible = false
            if (isAdded) {
                Toast.makeText(
                    requireContext(),
                    "Failed to initialize player",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    private fun setupClickListeners(short: Short) {
        binding.btnVolume.setOnClickListener {
            toggleVolume()
        }

        binding.btnLike.setOnClickListener {
            Toast.makeText(requireContext(), "Liked: ${short.title}", Toast.LENGTH_SHORT).show()
        }

        binding.btnComment.setOnClickListener {
            Toast.makeText(requireContext(), "Comments for: ${short.title}", Toast.LENGTH_SHORT)
                .show()
        }

        binding.btnShare.setOnClickListener {
            Toast.makeText(requireContext(), "Shared: ${short.title}", Toast.LENGTH_SHORT).show()
        }

//        binding.playerView.setOnClickListener {
//            Toast.makeText(requireContext(), "Tap to pause/play", Toast.LENGTH_SHORT).show()
//        }

        binding.playerView.setOnClickListener {
            togglePlayPause()
        }
    }

    private fun togglePlayPause() {
        player?.let { exoPlayer ->
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }
        }
    }

    private fun toggleVolume() {
        player?.let { exoPlayer ->
            isMuted = !isMuted
            if (isMuted) {
                lastVolume = exoPlayer.volume
                exoPlayer.volume = 0f
            } else {
                exoPlayer.volume = lastVolume
            }
            updateVolumeIcon()
        }
    }

    private fun updateVolumeIcon() {
        if (!isAdded || _binding == null) return

        val iconRes = if (isMuted) {
            R.drawable.ic_volume_off
        } else {
            R.drawable.ic_volume_on
        }
        binding.btnVolume.setImageResource(iconRes)
    }

    fun playVideo() {
        if (player == null) {
            Log.d(
                "${this::class.java.simpleName}",
                "${Throwable().stackTrace[0].methodName} ${short?.id}"
            )
        }

        player?.let { exoPlayer ->
            if (isPlayerReady) {
                exoPlayer.play()
            } else {
                shouldAutoPlay = true
            }
        } ?: run {
            short?.videoUrl?.let { url -> initializePlayer(url) }
            shouldAutoPlay = true
        }
    }

    fun pauseVideo() {
        player?.let { exoPlayer ->
            exoPlayer.pause()
        }
        shouldAutoPlay = false
    }

    fun isVideoPlaying(): Boolean {
        return player?.isPlaying ?: false
    }

    override fun onPause() {
        super.onPause()
        // pauseVideo()
        releasePlayer()

//        binding.playerView.setPlayer(null)

        Log.d(
            "${this::class.java.simpleName}",
            "${Throwable().stackTrace[0].methodName} ${short?.id}"
        )
    }

    override fun onResume() {
        super.onResume()

        if(player == null && short != null) {
            short?.videoUrl?.let {
                url -> initializePlayer(url)
            }
        }

        Log.d(
            "${this::class.java.simpleName}",
            "${Throwable().stackTrace[0].methodName} ${short?.id}"
        )
    }

    override fun onStop() {
        super.onStop()

        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()

//        releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
        _binding = null

//        for(i in 1..10000) {
//
//        }

        Log.d(
            "${this::class.java.simpleName}",
            "${Throwable().stackTrace[0].methodName} ${short?.id}"
        )
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            try {
                exoPlayer.stop()
                exoPlayer.release()
            } catch (e: Exception) {
                Log.e("${this::class.java.simpleName}", "Error releasing player: ${e.message}", e)
            }
        }

        player = null
        isPlayerReady = false
        shouldAutoPlay = false

        binding.playerView.setPlayer(null)
    }

    companion object {
        fun newInstance(short: Short): ShortVideoFragment {
            val fragment = ShortVideoFragment()
            val bundle = Bundle().apply {
                putParcelable("short", short)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}