package com.raywenderlich.videoplayerapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.raywenderlich.videoplayerapp.R
import com.raywenderlich.videoplayerapp.databinding.ActivityMainBinding
import com.raywenderlich.videoplayerapp.ui.fragments.HomeFragment
import com.raywenderlich.videoplayerapp.ui.fragments.ShortsFragment
import com.raywenderlich.videoplayerapp.ui.fragments.SubscriptionFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        // Load Home fragment by default
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_shorts -> {
                    loadFragment(ShortsFragment())
                    true
                }
                R.id.nav_subscription -> {
                    loadFragment(SubscriptionFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}