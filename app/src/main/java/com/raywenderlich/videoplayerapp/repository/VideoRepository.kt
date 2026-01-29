package com.raywenderlich.videoplayerapp.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.raywenderlich.videoplayerapp.model.Video
import kotlin.jvm.java

class VideoRepository {

    private val database = FirebaseDatabase.getInstance("https://video-player-app-e31b0-default-rtdb.firebaseio.com/")
    private val videosRef = database.getReference("videos")

    // Fetch all videos from Firebase Realtime Database
    fun getAllVideos(
        onSuccess: (List<Video>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        videosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val videoList = mutableListOf<Video>()

                for (videoSnapshot in snapshot.children) {
                    val video = videoSnapshot.getValue(Video::class.java)
                    video?.let { videoList.add(it) }
                }

                onSuccess(videoList)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }
}