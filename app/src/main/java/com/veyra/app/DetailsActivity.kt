package com.veyra.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class DetailsActivity : AppCompatActivity() {

    private var movieId: Int = 0
    private var movieTitle: String = ""
    private var isTvShow: Boolean = false

    private lateinit var seasonSpinner: Spinner
    private lateinit var episodeSpinner: Spinner
    private lateinit var watchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        movieId = intent.getIntExtra("MOVIE_ID", 0)
        movieTitle = intent.getStringExtra("MOVIE_TITLE") ?: ""
        isTvShow = intent.getBooleanExtra("IS_TV_SHOW", false)

        supportActionBar?.title = "Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        seasonSpinner = findViewById(R.id.seasonSpinner)
        episodeSpinner = findViewById(R.id.episodeSpinner)
        watchButton = findViewById(R.id.watchButton)

        if (isTvShow) {
            findViewById<LinearLayout>(R.id.tvControls).visibility = View.VISIBLE
            watchButton.text = "Watch Episode"

            seasonSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    fetchEpisodesForSeason(position + 1)
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        } else {
            watchButton.text = "Watch Movie"
        }

        watchButton.setOnClickListener { openPlayer() }
        fetchDetails()
    }

    private fun fetchDetails() {
        val apiKey = getString(R.string.tmdb_api_key)
        val type = if (isTvShow) "tv" else "movie"
        // MAGIC: append_to_response gets everything in one request!
        val urlString = "https://api.themoviedb.org/3/$type/$movieId?api_key=$apiKey&append_to_response=credits,reviews"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val jsonResult = URL(urlString).readText()
                val json = JSONObject(jsonResult)

                val title = json.optString("title", json.optString("name", movieTitle))
                val overview = json.optString("overview", "No description available.")
                val backdropPath = json.optString("backdrop_path", null)
                val posterPath = json.optString("poster_path", null)
                
                val date = json.optString("release_date", json.optString("first_air_date", ""))
                val year = if (date.length >= 4) date.substring(0, 4) else ""
                val rating = json.optDouble("vote_average", 0.0)

                // Parse Cast (Top 8)
                val castList = mutableListOf<CastMember>()
                val credits = json.optJSONObject("credits")
                val castArray = credits?.optJSONArray("cast")
                if (castArray != null) {
                    for (i in 0 until minOf(castArray.length(), 8)) {
                        val c = castArray.getJSONObject(i)
                        castList.add(CastMember(
                            name = c.optString("name", "Unknown"),
                            character = c.optString("character", "Unknown"),
                            profilePath = c.optString("profile_path", null)
                        ))
                    }
                }

                // Parse Reviews (Top 2)
                val reviewList = mutableListOf<Review>()
                val reviews = json.optJSONObject("reviews")
                val reviewArray = reviews?.optJSONArray("results")
                if (reviewArray != null) {
                    for (i in 0 until minOf(reviewArray.length(), 2)) {
                        val r = reviewArray.getJSONObject(i)
                        reviewList.add(Review(
                            author = r.optString("author", "Anonymous"),
                            content = r.optString("content", "No content").replace("\n", " ")
                        ))
                    }
                }

                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.titleText).text = title
                    findViewById<TextView>(R.id.overviewText).text = overview
                    findViewById<TextView>(R.id.infoText).text = "$year • ⭐ ${String.format("%.1f", rating)}"

                    val backdropImage = findViewById<ImageView>(R.id.backdropImage)
                    val posterImage = findViewById<ImageView>(R.id.posterImage)

                    if (backdropPath != null) backdropImage.load("https://image.tmdb.org/t/p/w780$backdropPath") { crossfade(true) }
                    if (posterPath != null) posterImage.load("https://image.tmdb.org/t/p/w500$posterPath") { crossfade(true) }

                    // Setup Cast
                    val castRecyclerView = findViewById<RecyclerView>(R.id.castRecyclerView)
                    castRecyclerView.layoutManager = LinearLayoutManager(this@DetailsActivity, LinearLayoutManager.HORIZONTAL, false)
                    castRecyclerView.adapter = CastAdapter(castList)

                    // Setup Reviews
                    val reviewRecyclerView = findViewById<RecyclerView>(R.id.reviewRecyclerView)
                    reviewRecyclerView.layoutManager = LinearLayoutManager(this@DetailsActivity)
                    reviewRecyclerView.adapter = ReviewAdapter(reviewList)

                    if (isTvShow) {
                        val numberOfSeasons = json.optInt("number_of_seasons", 1)
                        val seasonList = (1..numberOfSeasons).map { "Season $it" }
                        val adapter = ArrayAdapter(this@DetailsActivity, android.R.layout.simple_spinner_dropdown_item, seasonList)
                        seasonSpinner.adapter = adapter
                        fetchEpisodesForSeason(1)
                    }

                    findViewById<View>(R.id.progressBar).visibility = View.GONE
                    findViewById<View>(R.id.contentContainer).visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("VeyraDetails", "Error fetching details", e)
                withContext(Dispatchers.Main) {
                    findViewById<View>(R.id.progressBar).visibility = View.GONE
                    Toast.makeText(this@DetailsActivity, "Failed to load details.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchEpisodesForSeason(seasonNumber: Int) {
        val apiKey = getString(R.string.tmdb_api_key)
        val urlString = "https://api.themoviedb.org/3/tv/$movieId/season/$seasonNumber?api_key=$apiKey"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val jsonResult = URL(urlString).readText()
                val json = JSONObject(jsonResult)
                val episodesArray = json.optJSONArray("episodes")
                val episodeCount = episodesArray?.length() ?: 1

                val episodeList = (1..episodeCount).map { "Episode $it" }
                
                withContext(Dispatchers.Main) {
                    val adapter = ArrayAdapter(this@DetailsActivity, android.R.layout.simple_spinner_dropdown_item, episodeList)
                    episodeSpinner.adapter = adapter
                }
            } catch (e: Exception) {
                Log.e("VeyraDetails", "Error fetching episodes", e)
            }
        }
    }

    private fun openPlayer() {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("MOVIE_ID", movieId)
        intent.putExtra("MOVIE_TITLE", movieTitle)
        intent.putExtra("IS_TV_SHOW", isTvShow)
        
        if (isTvShow) {
            intent.putExtra("SEASON", seasonSpinner.selectedItemPosition + 1)
            intent.putExtra("EPISODE", episodeSpinner.selectedItemPosition + 1)
        }
        
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}