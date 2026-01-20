package com.raywenderlich.videoplayerapp.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.raywenderlich.videoplayerapp.R
import com.raywenderlich.videoplayerapp.databinding.FragmentShortVideoBinding
import com.raywenderlich.videoplayerapp.model.Short
import kotlin.math.exp

class ShortVideoFragment : Fragment() {
    private var _binding: FragmentShortVideoBinding? = null
    private val binding get() = _binding!!

    private var short: Short? = null

    private var player: ExoPlayer? = null
    private var isPlaying = false
    private var isMuted = true

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

        short = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("short", Short::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("short")
        }

        short?.let {
            setupUI(it)
        }
    }

    private fun setupUI(short: Short) {
        binding.tvTitle.text = short.title
        binding.tvChannelName.text = short.channelName
        binding.tvLike.text = short.likes
        binding.tvViews.text = short.views

        binding.progressBar.isVisible = true

        binding.root.postDelayed({
            binding.progressBar.isVisible = false
        }, 500)

        initializePlayer(short.videoUrl)

        setupClickListeners(short)
    }

    private fun initializePlayer(videoUrl: String) {
        binding.progressBar.isVisible = true

        player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer

            val mediaItem = MediaItem.fromUri(videoUrl)
            exoPlayer.setMediaItem(mediaItem)

            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

            exoPlayer.volume = 0f

            exoPlayer.prepare()

            exoPlayer.playWhenReady = false

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            binding.progressBar.isVisible = true
                        }
                        Player.STATE_READY -> {
                            binding.progressBar.isVisible = false
                        }
                        Player.STATE_ENDED -> {

                        }
                    }
                }
            })
        }

        updateVolumeIcon()
    }

    private fun setupClickListeners(short: Short) {
        binding.btnVolume.setOnClickListener {
            toggleVolume()
        }

        binding.btnLike.setOnClickListener {
            Toast.makeText(requireContext(), "Liked: ${short.title}", Toast.LENGTH_SHORT).show()
        }

        binding.btnComment.setOnClickListener {
            Toast.makeText(requireContext(), "Comments for: ${short.title}", Toast.LENGTH_SHORT).show()
        }

        binding.btnShare.setOnClickListener {
            Toast.makeText(requireContext(), "Shared: ${short.title}", Toast.LENGTH_SHORT).show()
        }

        binding.playerView.setOnClickListener {
            Toast.makeText(requireContext(), "Tap to pause/play", Toast.LENGTH_SHORT).show()
        }

        binding.playerView.setOnClickListener {
            togglePlayPause()
        }
    }

    private fun togglePlayPause() {
        player?.let { exoPlayer ->
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                isPlaying = false
            } else {
                exoPlayer.play()
                isPlaying = true
            }
        }
    }

    private fun toggleVolume() {
        player?.let { exoPlayer ->
            isMuted = !isMuted
            exoPlayer.volume = if (isMuted) 0f else 1f
            updateVolumeIcon()
        }
    }

    private fun updateVolumeIcon() {
        val iconRes = if (isMuted) {
            R.drawable.ic_volume_off
        } else {
            R.drawable.ic_volume_on
        }
        binding.btnVolume.setImageResource(iconRes)
    }

    fun playVideo() {
        player?.play()
        isPlaying = true
    }

    fun pauseVideo() {
        player?.pause()
        isPlaying = false
    }

    fun isVideoPlaying(): Boolean {
        return player?.isPlaying ?: false
    }

    override fun onPause() {
        super.onPause()
        // Pause video when fragment goes to background
        pauseVideo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
        _binding = null
    }

    private fun releasePlayer() {
        player?.release()
        player = null
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