package com.raywenderlich.videoplayerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raywenderlich.videoplayerapp.model.Channel

class SubscriptionViewModel : ViewModel() {
    private val _subscribedChannels = MutableLiveData<MutableList<Channel>>(mutableListOf())
    val subscribedChannels: LiveData<MutableList<Channel>> = _subscribedChannels

    init {
        _subscribedChannels.value = mutableListOf()
    }

    fun subscribeToChannel(channelName: String) {
        val currentList = _subscribedChannels.value ?: mutableListOf()

        if (!isSubscribed(channelName)) {
            val channel = Channel(channelName)
            currentList.add(channel)
            _subscribedChannels.value = currentList
        }
    }

    fun unsubscribeFromChannel(channelName: String) {
        val currentList = _subscribedChannels.value ?: mutableListOf()
        currentList.removeAll { it.name == channelName }
        _subscribedChannels.value = currentList
    }

    fun isSubscribed(channelName: String) : Boolean {
        return _subscribedChannels.value?.any { it.name == channelName } ?: false
    }

    fun getSubscribedChannels() : List<Channel> {
        return _subscribedChannels.value ?: emptyList()
    }

    fun getSubscriptionCount() : Int {
        return _subscribedChannels.value?.size ?: 0
    }
}