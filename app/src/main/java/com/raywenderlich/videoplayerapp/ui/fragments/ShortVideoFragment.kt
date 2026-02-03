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
import com.raywenderlich.videoplayerapp.utils.PlayerManager
import kotlinx.coroutines.delay
import kotlin.jvm.java
import kotlin.math.exp

class ShortVideoFragment : Fragment() {
    private var _binding: FragmentShortVideoBinding? = null
    private val binding get() = _binding!!

    private var short: Short? = null
    private val playerManager = PlayerManager

    private var isMuted = false
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
            setupPlayerCallbacks()
            setupClickListeners(shortData)
        }

        Log.d(
            "${this::class.java.simpleName}",
            "${Throwable().stackTrace[0].methodName} ${short?.id}"
        )
    }

    private fun setupUI(short: Short) {
        binding.playerView.useController = true
        binding.playerView.controllerShowTimeoutMs = 800

        binding.tvTitle.text = short.title
        binding.tvChannelName.text = short.channelName
        binding.tvLike.text = short.likes
        binding.tvViews.text = short.views

        binding.progressBar.isVisible = false

//        binding.root.postDelayed({
//            binding.progressBar.isVisible = false
//        }, 500)

//        binding.root.post({
//            binding.progressBar.isVisible = false
//        })

        updateVolumeIcon()
    }

    private fun setupPlayerCallbacks() {
        playerManager.setPlayerCallbacks(
            onReady = {
                if (isAdded && _binding != null) {
                    binding.progressBar.isVisible = false
                }
            },
            onBuffering = { isBuffering ->
                if (isAdded && _binding != null) {
                    binding.progressBar.isVisible = isBuffering
                }
            },
            onError = { errorMessage ->
                if (isAdded && _binding != null) {
                    binding.progressBar.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        "Error playing video: $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.root.postDelayed({
                        if (isAdded && _binding != null) {
                            short?.videoUrl?.let { url ->
                                attachPlayerToView(url, autoPlay = false)
                            }
                        }
                    }, 1000)
                }
            }
        )
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

        binding.playerView.setOnClickListener {
            togglePlayPause()
        }
    }

    private fun togglePlayPause() {
        if (playerManager.isPlaying()) {
            playerManager.pause()
        } else {
            playerManager.play()
        }
    }

    private fun toggleVolume() {
        isMuted = !isMuted

        if (isMuted) {
            lastVolume = playerManager.getVolume()
            playerManager.setVolume(0f)
        } else {
            playerManager.setVolume(lastVolume)
        }

        updateVolumeIcon()
    }

    private fun updateVolumeIcon() {
        if (!isAdded || _binding == null) return

        val iconRes = if (isMuted || playerManager.getVolume() == 0f) {
            R.drawable.ic_volume_off
        } else {
            R.drawable.ic_volume_on
        }
        binding.btnVolume.setImageResource(iconRes)
    }

    private fun attachPlayerToView(videoUrl: String, autoPlay: Boolean) {
        if (!isAdded || _binding == null) return

        binding.playerView.isVisible = true

        playerManager.attachPlayer(
            playerView = binding.playerView,
            videoUrl = videoUrl,
            autoPlay = autoPlay,
            context = requireContext()
        )

        Log.d(
            "${this::class.java.simpleName}",
            "Player attached for video: ${short?.id}"
        )
    }

    private fun detachPlayerFromView() {
        if (!isAdded || _binding == null) return

        playerManager.detachPlayer()

        Log.d(
            "${this::class.java.simpleName}",
            "Player detached from: ${short?.id}"
        )
    }

    fun playVideo() {
        short?.videoUrl?.let { url ->
            attachPlayerToView(url, autoPlay = true)
        }

        Log.d(
            "${this::class.java.simpleName}",
            "playVideo() called for: ${short?.id}"
        )

    }

    fun pauseVideo() {
        detachPlayerFromView()

        Log.d(
            "${this::class.java.simpleName}",
            "pauseVideo() called for: ${short?.id}"
        )
    }

    fun isVideoPlaying(): Boolean {
        return playerManager.isPlaying()
    }

    override fun onPause() {
        super.onPause()

        Log.d(
            "${this::class.java.simpleName}",
            "${Throwable().stackTrace[0].methodName} ${short?.id}"
        )
    }

    override fun onResume() {
        super.onResume()

        Log.d(
            "${this::class.java.simpleName}",
            "${Throwable().stackTrace[0].methodName} ${short?.id}"
        )
    }

    override fun onStop() {
        super.onStop()

        Log.d(
            "${this::class.java.simpleName}",
            "${Throwable().stackTrace[0].methodName} ${short?.id}"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        playerManager.clearCallbacks()

        detachPlayerFromView()

        _binding = null

        Log.d(
            "${this::class.java.simpleName}",
            "${Throwable().stackTrace[0].methodName} ${short?.id}"
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(
            "${this::class.java.simpleName}",
            "${Throwable().stackTrace[0].methodName} ${short?.id}"
        )
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