package com.veyra.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_drawer, R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        loadFragment(MoviesFragment())

        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_movies -> { 
                    loadFragment(MoviesFragment())
                    true 
                }
                R.id.nav_tv -> { 
                    loadFragment(TVShowsFragment())
                    true 
                }
                R.id.nav_recent -> { 
                    loadFragment(RecentFragment())
                    true 
                }
                else -> false
            }
        }

        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { menuItem: MenuItem ->
            drawerLayout.closeDrawer(GravityCompat.START)
            
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_about -> {
                    // 🔥 DYNAMIC VERSION: Pulls from build.gradle.kts automatically
                    Toast.makeText(this, "Veyra v${BuildConfig.VERSION_NAME}\nBuilt on Arch Linux 🐧\nLightweight & Fast", Toast.LENGTH_LONG).show()
                }
                R.id.nav_patchnotes -> {
                    showPatchNotes()
                }
                R.id.nav_credits -> {
                    Toast.makeText(this, "Developed by Clinton\nPowered by TMDB & VidNest", Toast.LENGTH_LONG).show()
                }
            }
            true
        }
    }

    private fun showPatchNotes() {
        // 🔥 DYNAMIC VERSION: Pulls from build.gradle.kts automatically
        val message = """
            🚀 Veyra v${BuildConfig.VERSION_NAME} Update
            
            ✨ New Features:
            • Netflix-style UI with Bottom Navigation
            • Auto-scrolling Hero Banners on Home tabs
            • Categorized horizontal lists (Trending, Top Rated, etc.)
            • Modern Sidebar with Header
            • Watch History tracking
            • In-app Update Checker with progress bar
            • "More Like This" recommendations
            • Genre chips and runtime display
            • Search bar on all tabs
            
            🎬 Details Page Upgrades:
            • Full Cast & Crew profiles with images
            • User Reviews section
            • Enhanced layout with backdrop & poster overlap
            
            🛠️ Optimizations:
            • Single API call for details + credits + reviews + recommendations (saves RAM!)
            • Aggressive popup ad blocking in player
            • True fullscreen immersive video mode
            • Dynamic versioning system (one source of truth)
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("📝 Patch Notes")
            .setMessage(message)
            .setPositiveButton("Awesome!", null)
            .show()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}