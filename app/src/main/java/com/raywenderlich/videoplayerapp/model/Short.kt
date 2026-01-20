package com.raywenderlich.videoplayerapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Short(
    val id: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val title: String = "",
    val channelName: String = "",
    val likes: String = "",
    val views: String = "",
    val uploadTime: String = ""
) : Parcelable
