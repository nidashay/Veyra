package com.veyra.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecentFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val recentClickListener: (Movie) -> Unit = { movie ->
            val intent = Intent(requireContext(), DetailsActivity::class.java)
            intent.putExtra("MOVIE_ID", movie.id)
            intent.putExtra("MOVIE_TITLE", movie.title)
            intent.putExtra("IS_TV_SHOW", movie.isTvShow)
            startActivity(intent)
        }

        loadHistory(recentClickListener)
    }

    private fun loadHistory(clickListener: (Movie) -> Unit) {
        progressBar.visibility = View.VISIBLE
        
        val history = WatchHistory.getHistory(requireContext())
        
        progressBar.visibility = View.GONE
        
        recyclerView.adapter = MovieAdapter(history, clickListener)
    }
}
