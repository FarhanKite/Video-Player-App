package com.raywenderlich.videoplayerapp.repository

import com.google.firebase.database.*
import com.google.firestore.v1.Value
import com.google.rpc.QuotaFailure
import com.raywenderlich.videoplayerapp.model.Short

class ShortsRepository {
    private val database = FirebaseDatabase.getInstance()
    private val shortsRef = database.getReference("shorts")

    fun getAllShorts(
        onSuccess: (List<Short>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        shortsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val shortsList = mutableListOf<Short>()

                for(childSnapshot in snapshot.children) {
                    val short = childSnapshot.getValue(Short::class.java)
                    short?.let { shortsList.add(it) }
                }

                onSuccess(shortsList)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }

    fun getShortById(
        shortId: String,
        onSuccess: (Short?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        shortsRef.child(shortId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val short = snapshot.getValue(Short::class.java)
                onSuccess(short)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }
        })
    }
}