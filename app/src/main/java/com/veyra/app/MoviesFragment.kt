package com.veyra.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MoviesFragment : Fragment() {

    private lateinit var bannerRecyclerView: RecyclerView
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var isAutoScrolling = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bannerRecyclerView = view.findViewById(R.id.bannerRecyclerView)
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)

        // Snap helper makes the banner snap to the center like Netflix
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(bannerRecyclerView)

        val clickListener: (Movie) -> Unit = { movie ->
            val intent = Intent(requireContext(), DetailsActivity::class.java)
            intent.putExtra("MOVIE_ID", movie.id)
            intent.putExtra("MOVIE_TITLE", movie.title)
            intent.putExtra("IS_TV_SHOW", false)
            startActivity(intent)
        }

        fetchData(clickListener)
        startAutoScroll()
    }

    private fun fetchData(clickListener: (Movie) -> Unit) {
        progressBar.visibility = View.VISIBLE
        val apiKey = requireContext().getString(R.string.tmdb_api_key)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val popularJson = URL("https://api.themoviedb.org/3/movie/popular?api_key=$apiKey&language=en-US&page=1").readText()
                val topRatedJson = URL("https://api.themoviedb.org/3/movie/top_rated?api_key=$apiKey&language=en-US&page=1").readText()
                val upcomingJson = URL("https://api.themoviedb.org/3/movie/upcoming?api_key=$apiKey&language=en-US&page=1").readText()

                val bannerMovies = parseMovies(popularJson, false).take(5)
                val categories = listOf(
                    MovieCategory("Trending Now", parseMovies(popularJson, false)),
                    MovieCategory("Top Rated", parseMovies(topRatedJson, false)),
                    MovieCategory("Upcoming", parseMovies(upcomingJson, false))
                )

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    bannerRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    bannerRecyclerView.adapter = BannerAdapter(bannerMovies, clickListener)
                    
                    categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    categoriesRecyclerView.adapter = CategoryAdapter(categories, clickListener)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to load data.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseMovies(jsonString: String, isTvShow: Boolean): List<Movie> {
        val jsonObject = JSONObject(jsonString)
        val resultsArray = jsonObject.getJSONArray("results")
        val movieList = mutableListOf<Movie>()
        for (i in 0 until resultsArray.length()) {
            val itemJson = resultsArray.getJSONObject(i)
            movieList.add(
                Movie(
                    id = itemJson.getInt("id"),
                    title = itemJson.getString("title"),
                    posterPath = itemJson.optString("poster_path", null),
                    isTvShow = isTvShow
                )
            )
        }
        return movieList
    }

    private fun startAutoScroll() {
        lifecycleScope.launch {
            while (isAutoScrolling) {
                delay(4000) // Scroll every 4 seconds
                if (isAdded && bannerRecyclerView.adapter != null && bannerRecyclerView.adapter!!.itemCount > 1) {
                    val layoutManager = bannerRecyclerView.layoutManager as LinearLayoutManager
                    val currentItem = layoutManager.findFirstVisibleItemPosition()
                    val nextItem = if (currentItem == bannerRecyclerView.adapter!!.itemCount - 1) 0 else currentItem + 1
                    bannerRecyclerView.smoothScrollToPosition(nextItem)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isAutoScrolling = false // Stop scrolling when user leaves the tab to save battery/RAM
    }
}