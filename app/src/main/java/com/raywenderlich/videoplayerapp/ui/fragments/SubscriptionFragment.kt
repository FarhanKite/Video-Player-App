package com.raywenderlich.videoplayerapp.ui.fragments

import android.R.attr.category
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.videoplayerapp.adapter.CategoryAdapter
import com.raywenderlich.videoplayerapp.adapter.SubscriptionAdapter
import com.raywenderlich.videoplayerapp.adapter.VideoAdapter
import com.raywenderlich.videoplayerapp.databinding.FragmentSubscriptionBinding
import com.raywenderlich.videoplayerapp.model.Channel
import com.raywenderlich.videoplayerapp.model.Video
import com.raywenderlich.videoplayerapp.repository.VideoRepository
import com.raywenderlich.videoplayerapp.ui.VideoPlayerActivity
import com.raywenderlich.videoplayerapp.viewmodel.SubscriptionViewModel

class SubscriptionFragment : Fragment() {

    private var _binding: FragmentSubscriptionBinding? = null
    private val binding get() = _binding!!

    private val subscriptionViewModel: SubscriptionViewModel by activityViewModels()

    private lateinit var subscriptionAdapter: SubscriptionAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var videoAdapter: VideoAdapter

    private val videoRepository = VideoRepository()

    private val categories = listOf("All", "Music", "Gaming", "News", "Sports", "Education")
    private var allVideos = listOf<Video>()
    private var filteredVideos = listOf<Video>()
    private var currentCategory = "All"

    private val channels = listOf(
        "John's Thought",
        "Travel With Mike",
        "Demo Channel 1",
        "Code Club",
        "House Vlog",
        "Code and Coffee",
        "Demo Channel 2",
        "Demo Channel 3",
        "East",
        "West",
        "North",
        "South",
        "John's Thought",
        "Travel With Mike",
        "Demo Channel 1",
        "Code Club",
        "House Vlog",
        "John's Thought",
        "Travel With Mike",
        "Demo Channel 1",
        "Code Club",
        "House Vlog",
        "John's Thought",
        "Travel With Mike",
        "Demo Channel 1",
        "Code Club",
        "House Vlog",
        "John's Thought",
        "Travel With Mike",
        "Demo Channel 1",
        "Code Club",
        "House Vlog",
        "John's Thought",
        "Travel With Mike",
        "Demo Channel 1",
        "Code Club",
        "House Vlog",
        "John's Thought",
        "Travel With Mike",
        "Demo Channel 1",
        "Code Club",
        "House Vlog"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChannelsRecyclerView()
        setupCategoriesRecyclerView()
        setupVideosRecyclerView()
        observeSubscriptions()
    }

    private fun setupChannelsRecyclerView() {
        subscriptionAdapter = SubscriptionAdapter(emptyList(), { channel ->
            handleUnsubscribe(channel)
        })

        binding.rvSubscribedChannels.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvSubscribedChannels.adapter = subscriptionAdapter
    }

    private fun setupCategoriesRecyclerView() {
        categoryAdapter = CategoryAdapter(categories) { category ->
            currentCategory = category
            filterVideosByCategory(category)
        }

        binding.rvCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = categoryAdapter
    }

    private fun setupVideosRecyclerView() {
        videoAdapter = VideoAdapter(emptyList(), { video ->
            openVideoPlayer(video)
        }, subscriptionViewModel, false)

        binding.rvVideos.layoutManager = LinearLayoutManager(context)
        binding.rvVideos.adapter = videoAdapter
    }

    fun handleUnsubscribe(channel: Channel) {
        subscriptionViewModel.unsubscribeFromChannel(channel.name)
        Toast.makeText(requireContext(), "Unsubscribed from $channel.name", Toast.LENGTH_SHORT).show()
    }

    private fun observeSubscriptions() {
        subscriptionViewModel.subscribedChannels.observe(viewLifecycleOwner) {
            channels -> subscriptionAdapter.updateChannels(channels)

            if(channels.isEmpty()) {
                showEmptyState(true)
            } else {
                showEmptyState(false)
                loadVideosFromFirebase()
            }
        }
    }

    private fun loadVideosFromFirebase() {
        videoRepository.getAllVideos(
            onSuccess = {videos ->
                allVideos = videos
                filterVideosFromSubscribedChannels()
            },
            onFailure = { errorMessage ->
                Toast.makeText(requireContext(), "Error loading videos: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun  filterVideosFromSubscribedChannels() {
        val subscribedChannelNames = subscriptionViewModel.getSubscribedChannels().map{ it.name }.toSet()

        filteredVideos = allVideos.filter { video ->
            subscribedChannelNames.contains(video.channelName)
        }

        filterVideosByCategory(currentCategory)
    }

    private fun filterVideosByCategory(category: String) {
        val displayVideos = if (category == "All") {
            filteredVideos
        } else {
            filteredVideos.filter { it.category == category }
        }

        videoAdapter.updateVideos(displayVideos)

        if(displayVideos.isEmpty() && filteredVideos.isNotEmpty()) {
            Toast.makeText(requireContext(), "No $category videos from subscriptions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openVideoPlayer(video: Video) {
        val intent = Intent(requireContext(), VideoPlayerActivity::class.java).apply {
            putExtra("VIDEO", video)
        }
        startActivity(intent)
    }

    private fun showEmptyState(show: Boolean) {
        if(show) {
            binding.ivEmptyIcon.isVisible = true
            binding.tvEmptyTitle.isVisible = true
            binding.tvEmptyDescription.isVisible = true

            binding.rvSubscribedChannels.isVisible = false
            binding.rvCategories.isVisible = false
            binding.rvVideos.isVisible = false
        } else {
            binding.ivEmptyIcon.isVisible = false
            binding.tvEmptyTitle.isVisible = false
            binding.tvEmptyDescription.isVisible = false

            binding.rvSubscribedChannels.isVisible = true
            binding.rvCategories.isVisible = true
            binding.rvVideos.isVisible = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}