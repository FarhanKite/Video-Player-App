package com.raywenderlich.videoplayerapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.videoplayerapp.adapter.CategoryAdapter
import com.raywenderlich.videoplayerapp.adapter.VideoAdapter
import com.raywenderlich.videoplayerapp.databinding.FragmentHomeBinding
import com.raywenderlich.videoplayerapp.model.Video
import com.raywenderlich.videoplayerapp.repository.VideoRepository
import com.raywenderlich.videoplayerapp.ui.VideoPlayerActivity
import com.raywenderlich.videoplayerapp.viewmodel.SubscriptionViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val subscriptionViewModel: SubscriptionViewModel by activityViewModels()

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
        videoAdapter = VideoAdapter(
            emptyList(),
            { video ->
                openVideoPlayer(video)
            },
            subscriptionViewModel,
            true
        )

        val layoutManager = LinearLayoutManager(context)

        binding.rvVideos.apply {
            this.layoutManager = layoutManager
            adapter = videoAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        checkVisibilityAndAutoPlay(layoutManager)
                    }
                }
            })
        }
    }

    private fun checkVisibilityAndAutoPlay(layoutManager: LinearLayoutManager) {

        if(!isAdded || _binding == null) return

        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

        if (firstVisiblePosition == RecyclerView.NO_POSITION) return

        var maxVisiblePercentage = 0f
        var mostVisiblePosition = -1

        for (position in firstVisiblePosition..lastVisiblePosition) {
            val view = layoutManager.findViewByPosition(position) ?: continue

            val visiblePercentage = getVisiblePercentage(view)

            if (visiblePercentage > maxVisiblePercentage) {
                maxVisiblePercentage = visiblePercentage
                mostVisiblePosition = position
            }
        }

        if (mostVisiblePosition != -1 && maxVisiblePercentage >= 0.7f) {
            val viewHolder = binding.rvVideos.findViewHolderForAdapterPosition(mostVisiblePosition) as? VideoAdapter.VideoViewHolder

            viewHolder?.let {
                videoAdapter.onViewHolderVisible(it)
            }
        } else {
            videoAdapter.pauseAllVideos()
        }
    }

    private fun getVisiblePercentage(view: View): Float {
        val rect = android.graphics.Rect()
        val isVisible = view.getGlobalVisibleRect(rect)

        if (!isVisible) return 0f

        val visibleHeight = rect.height()
        val totalHeight = view.height

        if (totalHeight > 0) {
            return visibleHeight.toFloat() / totalHeight
        } else {
            return 0f
        }
    }

    private fun loadVideosFromFirebase() {
        if (!isAdded || _binding == null) return

        binding.progressBar.isVisible = true

        videoRepository.getAllVideos(
            onSuccess = { videos ->
                if (!isAdded || _binding == null) return@getAllVideos

                binding.progressBar.isVisible = false

                allVideos = videos
                videoAdapter.updateVideos(allVideos)

                if (videos.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "No videos found. Please add videos to Firebase.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (isAdded && _binding != null) {
                        binding.rvVideos.post {
                            val layoutManager =
                                binding.rvVideos.layoutManager as? LinearLayoutManager
                            layoutManager?.let {
                                checkVisibilityAndAutoPlay(it)
                            }
                        }
                    }
                }
            },
            onFailure = { errorMessage ->
                if (!isAdded || _binding == null) return@getAllVideos

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
        if(!isAdded || _binding == null) return

        val filteredVideos = if (category == "All") {
            allVideos
        } else {
            allVideos.filter { it.category == category }
        }

        videoAdapter.updateVideos(filteredVideos)
    }

    private fun openVideoPlayer(video: Video, isAutoPlay: Boolean = false) {
        val intent = Intent(requireContext(), VideoPlayerActivity::class.java).apply {
            putExtra("VIDEO", video)
        }
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()

        videoAdapter.pauseAllVideos()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}