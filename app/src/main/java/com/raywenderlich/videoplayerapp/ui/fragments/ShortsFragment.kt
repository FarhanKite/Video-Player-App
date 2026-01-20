package com.raywenderlich.videoplayerapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.raywenderlich.videoplayerapp.adapter.ShortsAdapter
import com.raywenderlich.videoplayerapp.databinding.FragmentShortsBinding
import com.raywenderlich.videoplayerapp.model.Short

class ShortsFragment : Fragment() {

    private var _binding: FragmentShortsBinding? = null
    private val binding get() = _binding!!

    private lateinit var shortsAdapter: ShortsAdapter

    private var shortsList = listOf<Short>()

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
        loadSampleShorts()
    }

    private fun setupViewPager() {
        shortsAdapter = ShortsAdapter(this, emptyList())
        binding.vpShorts.adapter = shortsAdapter

        binding.vpShorts.offscreenPageLimit = 1
    }

    private fun loadSampleShorts() {
        binding.progressBar.isVisible = true

        shortsList = listOf(
            Short(
                id = "short1",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                thumbnailUrl = "https://picsum.photos/400/600?random=1",
                title = "Amazing dance moves! 🔥",
                channelName = "Dance Master",
                likes = "1.2K",
                views = "10K views",
                uploadTime = "2 days ago"
            ),
            Short(
                id = "short2",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                thumbnailUrl = "https://picsum.photos/400/600?random=2",
                title = "Epic travel adventure",
                channelName = "Travel Vlogs",
                likes = "2.5K",
                views = "15K views",
                uploadTime = "1 day ago"
            ),
            Short(
                id = "short3",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                thumbnailUrl = "https://picsum.photos/400/600?random=3",
                title = "Hilarious comedy sketch 😂",
                channelName = "Fun Channel",
                likes = "5.8K",
                views = "50K views",
                uploadTime = "3 hours ago"
            ),
            Short(
                id = "short4",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
                thumbnailUrl = "https://picsum.photos/400/600?random=4",
                title = "Extreme sports action! 🏂",
                channelName = "Thrill Seekers",
                likes = "3.4K",
                views = "25K views",
                uploadTime = "5 hours ago"
            ),
            Short(
                id = "short5",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
                thumbnailUrl = "https://picsum.photos/400/600?random=5",
                title = "Cooking tips and tricks 👨‍🍳",
                channelName = "Chef's Kitchen",
                likes = "1.9K",
                views = "12K views",
                uploadTime = "1 hour ago"
            ),
            Short(
                id = "short6",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                thumbnailUrl = "https://picsum.photos/400/600?random=6",
                title = "Cute animals compilation 🐰",
                channelName = "Animal Lovers",
                likes = "8.2K",
                views = "80K views",
                uploadTime = "6 hours ago"
            ),
            Short(
                id = "short7",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                thumbnailUrl = "https://picsum.photos/400/600?random=7",
                title = "Mind-blowing magic trick ✨",
                channelName = "Magic Show",
                likes = "4.7K",
                views = "35K views",
                uploadTime = "2 hours ago"
            ),
            Short(
                id = "short8",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                thumbnailUrl = "https://picsum.photos/400/600?random=8",
                title = "Beautiful sunset timelapse 🌅",
                channelName = "Nature Films",
                likes = "6.1K",
                views = "45K views",
                uploadTime = "4 hours ago"
            )
        )

        shortsAdapter.updateShorts(shortsList)

        binding.progressBar.isVisible = false

        if(shortsList.isEmpty()) {
            Toast.makeText(requireContext(), "No shorts available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}