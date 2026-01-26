package com.raywenderlich.videoplayerapp.ui.fragments

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.animation.Positioning
import com.raywenderlich.videoplayerapp.adapter.ShortsAdapter
import com.raywenderlich.videoplayerapp.databinding.FragmentShortsBinding
import com.raywenderlich.videoplayerapp.model.Short
import com.raywenderlich.videoplayerapp.repository.ShortsRepository

class ShortsFragment : Fragment() {

    private var _binding: FragmentShortsBinding? = null
    private val binding get() = _binding!!

    private lateinit var shortsAdapter: ShortsAdapter
    private val shortsRepository = ShortsRepository()
    private var shortsList = listOf<Short>()
    private var currentPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShortsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        loadShortsFromFirebase()
    }

    private fun setupViewPager() {
        if (!isAdded || _binding == null) return

        shortsAdapter = ShortsAdapter(this, emptyList())
        binding.vpShorts.adapter = shortsAdapter

        binding.vpShorts.offscreenPageLimit = 1

        binding.vpShorts.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (!isAdded || _binding == null) return

                pauseVideoAtPosition(currentPosition)

                playVideoAtPosition(position)

                currentPosition = position
            }
        })
    }

    private fun playVideoAtPosition(position: Int) {
        if (!isAdded || _binding == null) return

        val fragment = getFragmentAtPosition(position)
        fragment?.playVideo()
    }

    private fun pauseVideoAtPosition(position: Int) {
        if (!isAdded || _binding == null) return

        val fragment = getFragmentAtPosition(position)
        fragment?.pauseVideo()
    }

    private fun getFragmentAtPosition(position: Int) : ShortVideoFragment? {
        if (!isAdded || _binding == null) return null

        val fragmentTag = "f$position"
        return childFragmentManager.findFragmentByTag(fragmentTag) as? ShortVideoFragment
    }

    private fun loadShortsFromFirebase() {
        if (!isAdded || _binding == null) return

        binding.progressBar.isVisible = true

        shortsRepository.getAllShorts(
            onSuccess = { shorts ->
                if (!isAdded || _binding == null) return@getAllShorts

                binding.progressBar.isVisible = false

                if(shorts.isEmpty()) {
                    Toast.makeText(requireContext(), "No shorts available", Toast.LENGTH_SHORT).show()
                } else {
                    shortsList = shorts
                    shortsAdapter.updateShorts(shortsList)

                    binding.vpShorts.post {
                        playVideoAtPosition(0)
                    }
                }
            },
            onFailure = { errorMessage ->
                if(!isAdded || _binding == null) return@getAllShorts

                binding.progressBar.isVisible = false

                Toast.makeText(requireContext(), "Error laoding shorts: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onPause() {
        super.onPause()
        pauseVideoAtPosition(currentPosition)
    }

    override fun onResume() {
        super.onResume()
        playVideoAtPosition(currentPosition)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}