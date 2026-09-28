package com.veyra.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class MoviePosterAdapter(
    private val movies: List<Movie>,
    private val onItemClick: (Movie) -> Unit
) : RecyclerView.Adapter<MoviePosterAdapter.PosterViewHolder>() {

    class PosterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImage: ImageView = itemView.findViewById(R.id.posterImage)
        val posterTitle: TextView = itemView.findViewById(R.id.posterTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie_poster, parent, false)
        return PosterViewHolder(view)
    }

    override fun onBindViewHolder(holder: PosterViewHolder, position: Int) {
        val movie = movies[position]
        holder.posterTitle.text = movie.title
        
        if (movie.getFullPosterUrl().isNotEmpty()) {
            holder.posterImage.load(movie.getFullPosterUrl()) {
                crossfade(true)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(movie) }
    }

    override fun getItemCount(): Int = movies.size
}