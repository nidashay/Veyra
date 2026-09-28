package com.veyra.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecentFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchEditText: EditText
    private var fullHistory: List<Movie> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        searchEditText = view.findViewById(R.id.searchEditText)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val recentClickListener: (Movie) -> Unit = { movie ->
            val intent = Intent(requireContext(), DetailsActivity::class.java)
            intent.putExtra("MOVIE_ID", movie.id)
            intent.putExtra("MOVIE_TITLE", movie.title)
            intent.putExtra("IS_TV_SHOW", movie.isTvShow)
            startActivity(intent)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                if (query.isEmpty()) {
                    updateAdapter(fullHistory, recentClickListener)
                } else {
                    val filtered = fullHistory.filter { it.title.lowercase().contains(query) }
                    updateAdapter(filtered, recentClickListener)
                }
            }
        })

        loadHistory(recentClickListener)
    }

    private fun loadHistory(clickListener: (Movie) -> Unit) {
        progressBar.visibility = View.VISIBLE
        fullHistory = WatchHistory.getHistory(requireContext())
        progressBar.visibility = View.GONE
        updateAdapter(fullHistory, clickListener)
    }

    private fun updateAdapter(history: List<Movie>, clickListener: (Movie) -> Unit) {
        recyclerView.adapter = MovieAdapter(history, clickListener)
    }
}