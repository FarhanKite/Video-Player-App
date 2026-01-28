package com.raywenderlich.videoplayerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NavigationViewModel : ViewModel() {
    private val _navigateToShort = MutableLiveData<String?>()
    val navigateToShort: MutableLiveData<String?> = _navigateToShort

    fun requestNavigateToShort(shortId: String) {
        _navigateToShort.value = shortId
    }

    fun clearNavigationRequest() {
        _navigateToShort.value = null
    }
}