package com.raywenderlich.videoplayerapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.videoplayerapp.adapter.CategoryAdapter
import com.raywenderlich.videoplayerapp.adapter.VideoAdapter
import com.raywenderlich.videoplayerapp.databinding.FragmentHomeBinding
import com.raywenderlich.videoplayerapp.model.Video
import com.raywenderlich.videoplayerapp.repository.VideoRepository
import com.raywenderlich.videoplayerapp.ui.VideoPlayerActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var videoAdapter: VideoAdapter
    private val videoRepository = VideoRepository()

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
        loadVideosFromFirebase()
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

    private fun loadVideosFromFirebase() {
        // Show loading
        binding.progressBar.isVisible = true

        videoRepository.getAllVideos(
            onSuccess = { videos ->
                // Hide loading
                binding.progressBar.isVisible = false

                allVideos = videos
                videoAdapter.updateVideos(allVideos)

                if (videos.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "No videos found. Please add videos to Firebase.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            onFailure = { errorMessage ->
                // Hide loading
                binding.progressBar.isVisible = false

                Toast.makeText(
                    requireContext(),
                    "Error: $errorMessage",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
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