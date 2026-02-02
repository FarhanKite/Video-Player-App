package com.raywenderlich.videoplayerapp.ui.fragments

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.animation.Positioning
import com.raywenderlich.videoplayerapp.adapter.ShortsAdapter
import com.raywenderlich.videoplayerapp.databinding.FragmentShortsBinding
import com.raywenderlich.videoplayerapp.model.Short
import com.raywenderlich.videoplayerapp.repository.ShortsRepository
import com.raywenderlich.videoplayerapp.viewmodel.NavigationViewModel

class ShortsFragment : Fragment() {

    private var _binding: FragmentShortsBinding? = null
    private val binding get() = _binding!!

    private lateinit var shortsAdapter: ShortsAdapter
    private val shortsRepository = ShortsRepository()

    private val navigationViewModel: NavigationViewModel by activityViewModels()
    private var shortsList = listOf<Short>()
    private var currentPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = null

        _binding = FragmentShortsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        loadShortsFromFirebase()

        observeNavigation()
    }

    private fun setupViewPager() {
        if (!isAdded || _binding == null) return

        shortsAdapter = ShortsAdapter(this, emptyList())
        binding.vpShorts.adapter = shortsAdapter

        binding.vpShorts.offscreenPageLimit = -1

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

//        binding.root.postDelayed({
//            if (!isAdded || _binding == null) return@postDelayed
//
//            val fragment = getFragmentAtPosition(position)
//            if (fragment != null) {
//                fragment.playVideo()
//                Log.d("", "Playing video at position: $position")
//            } else {
//                Log.d("", "Fragment not found at position: $position")
//
//                binding.root.postDelayed({
//                    if (isAdded && _binding != null) {
//                        getFragmentAtPosition(position)?.playVideo()
//                    }
//                }, 300)
//            }
//        }, 100)

        binding.root.post({
            val fragment = getFragmentAtPosition(position)
            if (fragment != null) {
                fragment.playVideo()
                Log.d("", "Playing video at position: $position")
            } else {
                Log.d("", "Fragment not found at position: $position")

//                binding.root.postDelayed({
//                    if (isAdded && _binding != null) {
//                        getFragmentAtPosition(position)?.playVideo()
//                    }
//                }, 300)
            }
        })
    }

    private fun pauseVideoAtPosition(position: Int) {
        if (!isAdded || _binding == null) return

        val fragment = getFragmentAtPosition(position)
        fragment?.pauseVideo()
        Log.d("", "Pausing video at position: $position")
    }

    private fun getFragmentAtPosition(position: Int) : ShortVideoFragment? {
        if (!isAdded || _binding == null) return null

        val fragmentTag = "f$position"
        val fragment = childFragmentManager.findFragmentByTag(fragmentTag) as? ShortVideoFragment

        if (fragment == null) {
            Log.d("", "Fragment not found for position: $position")
        }

        return fragment
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

//                    binding.vpShorts.post {
//                        playVideoAtPosition(0)
//                    }
//                    binding.vpShorts.postDelayed({
//                        if (isAdded && _binding != null) {
//                            playVideoAtPosition(0)
//                        }
//                    }, 300)

                    binding.vpShorts.post({
                        if (isAdded && _binding != null) {
                            playVideoAtPosition(0)
                        }
                    })
                }

                // observe navigation when data is loaded successfully
                observeNavigation()
            },
            onFailure = { errorMessage ->
                if(!isAdded || _binding == null) return@getAllShorts

                binding.progressBar.isVisible = false

                Toast.makeText(requireContext(), "Error laoding shorts: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun observeNavigation() {
        navigationViewModel.navigateToShort.observe(viewLifecycleOwner) { shortId ->
            if (shortId != null && shortsList.isNotEmpty()) {
                jumpToShort(shortId)
                navigationViewModel.clearNavigationRequest()
            }
        }
    }

    private fun jumpToShort(shortId: String) {

        if (!isAdded || _binding == null) return

        val position = shortsList.indexOfFirst { it.id == shortId }

        if(position != -1) {
            binding.vpShorts.setCurrentItem(position, false)

//            binding.vpShorts.post {
//                if (isAdded && _binding != null) {
//                    playVideoAtPosition(position)
//                    currentPosition = position
//                }
//            }

            binding.vpShorts.postDelayed({
                if (isAdded && _binding != null) {
                    playVideoAtPosition(position)
                    currentPosition = position
                }
            }, 500)
        } else {
            Toast.makeText(requireContext(), "Short not found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        pauseVideoAtPosition(currentPosition)
    }

    override fun onResume() {
        super.onResume()
        playVideoAtPosition(currentPosition)

        Log.d("${this::class.java.simpleName}", "${Throwable().stackTrace[0].methodName}")
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
//        super.onDestroyView()
        _binding = null

        val fragmentCount = childFragmentManager.fragments.size
        Log.e("${this::class.java.simpleName}", "Fragments still alive: $fragmentCount")

//        binding.vpShorts.adapter = null
        Log.d("${this::class.java.simpleName}", "${Throwable().stackTrace[0].methodName}")

        super.onDestroyView()
    }

    override fun onDestroy() {
        _binding = null

        val fragmentCount = childFragmentManager.fragments.size
        Log.e("${this::class.java.simpleName}", "Fragments still alive: $fragmentCount")

//        binding.vpShorts.adapter = null
        Log.d("${this::class.java.simpleName}", "${Throwable().stackTrace[0].methodName}")

        super.onDestroy()
    }
}