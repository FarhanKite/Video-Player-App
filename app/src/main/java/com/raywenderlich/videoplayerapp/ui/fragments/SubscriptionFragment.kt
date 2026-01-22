package com.raywenderlich.videoplayerapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.videoplayerapp.adapter.SubscriptionAdapter
import com.raywenderlich.videoplayerapp.databinding.FragmentSubscriptionBinding
import com.raywenderlich.videoplayerapp.model.Channel
import com.raywenderlich.videoplayerapp.viewmodel.SubscriptionViewModel

class SubscriptionFragment : Fragment() {

    private var _binding: FragmentSubscriptionBinding? = null
    private val binding get() = _binding!!

    private val subscriptionViewModel: SubscriptionViewModel by activityViewModels()

    private lateinit var subscriptionAdapter: SubscriptionAdapter

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

        setupRecyclerView()
        observeSubscriptions()
    }

    private fun setupRecyclerView() {
        subscriptionAdapter = SubscriptionAdapter(emptyList(), { channel ->
            handleUnsubscribe(channel)
        })

        binding.rvSubscriptions.layoutManager = LinearLayoutManager(context)
        binding.rvSubscriptions.adapter = subscriptionAdapter
    }

    fun handleUnsubscribe(channel: Channel) {
        subscriptionViewModel.unsubscribeFromChannel(channel.name)
        Toast.makeText(requireContext(), "Unsubscribed from $channel.name", Toast.LENGTH_SHORT).show()
    }

    private fun observeSubscriptions() {
        subscriptionViewModel.subscribedChannels.observe(viewLifecycleOwner) {
            channels -> subscriptionAdapter.updateChannels(channels)

            if(channels.isEmpty()) {
                binding.ivEmptyIcon.isVisible = true
                binding.tvEmptyTitle.isVisible = true
                binding.tvEmptyDescription.isVisible = true
                binding.rvSubscriptions.isVisible = false
            } else {
                binding.ivEmptyIcon.isVisible = false
                binding.tvEmptyTitle.isVisible = false
                binding.tvEmptyDescription.isVisible = false
                binding.rvSubscriptions.isVisible = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}