package com.raywenderlich.videoplayerapp.ui.fragments

//import kotlin.jvm.java

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.videoplayerapp.adapter.CategoryAdapter
import com.raywenderlich.videoplayerapp.adapter.VideoAdapter
import com.raywenderlich.videoplayerapp.databinding.FragmentHomeBinding
import com.raywenderlich.videoplayerapp.model.Video
import com.raywenderlich.videoplayerapp.ui.VideoPlayerActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var videoAdapter: VideoAdapter

    private val categories = listOf("All", "Music", "Gaming", "News", "Sports", "Education")
    private var allVideos = listOf<Video>()
    private var currentCategory = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryRecyclerView()
        setupVideoRecyclerView()
        loadSampleVideos()
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryAdapter(categories) { category ->
            currentCategory = category
            filterVideos(category)
        }

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    private fun setupVideoRecyclerView() {
        videoAdapter = VideoAdapter(emptyList()) { video ->
            openVideoPlayer(video)
        }

        binding.rvVideos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = videoAdapter
        }
    }

    private fun loadSampleVideos() {
        // Sample data - Replace with Firebase data later
        allVideos = listOf(
            Video(
                id = "1",
                title = "Amazing Sunset Timelapse",
                description = "Beautiful sunset captured in 4K",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                thumbnailUrl = "https://picsum.photos/400/300",
                category = "All",
                channelName = "Nature Channel",
                views = "1.2M views",
                uploadTime = "2 days ago"
            ),
            Video(
                id = "2",
                title = "Epic Gaming Moments",
                description = "Best gaming highlights",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                thumbnailUrl = "https://picsum.photos/401/300",
                category = "Gaming",
                channelName = "Game Master",
                views = "850K views",
                uploadTime = "5 days ago"
            ),
            Video(
                id = "3",
                title = "Relaxing Music for Study",
                description = "2 hours of peaceful music",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                thumbnailUrl = "https://picsum.photos/402/300",
                category = "Music",
                channelName = "Music Vibes",
                views = "3.5M views",
                uploadTime = "1 week ago"
            ),
            Video(
                id = "4",
                title = "Breaking News Today",
                description = "Latest news updates",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                thumbnailUrl = "https://picsum.photos/403/300",
                category = "News",
                channelName = "News Network",
                views = "2.1M views",
                uploadTime = "3 hours ago"
            ),
            Video(
                id = "5",
                title = "Football Highlights",
                description = "Best goals of the season",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                thumbnailUrl = "https://picsum.photos/404/300",
                category = "Sports",
                channelName = "Sports Hub",
                views = "4.5M views",
                uploadTime = "1 day ago"
            )
        )

        videoAdapter.updateVideos(allVideos)
    }

    private fun filterVideos(category: String) {
        val filteredVideos = if (category == "All") {
            allVideos
        } else {
            allVideos.filter { it.category == category }
        }
        videoAdapter.updateVideos(filteredVideos)
    }

    private fun openVideoPlayer(video: Video) {
        val intent = Intent(requireContext(), VideoPlayerActivity::class.java).apply {
            putExtra("VIDEO", video)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}