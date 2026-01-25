package com.raywenderlich.videoplayerapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Video(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val category: String = "",
    val channelName: String = "",
    val channelAvatar: String = "",
    val views: String = "",
    val uploadTime: String = ""
) : Parcelable