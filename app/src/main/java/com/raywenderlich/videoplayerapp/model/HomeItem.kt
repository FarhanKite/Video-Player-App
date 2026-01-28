package com.raywenderlich.videoplayerapp.model

import com.google.common.primitives.Shorts

sealed class HomeItem {
    data class VideoItem(val video: Video) : HomeItem()
    data class ShortsGridItem(val shorts: List<Short>) : HomeItem()
}