package com.veyra.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class BannerAdapter(
    private val movies: List<Movie>,
    private val onItemClick: (Movie) -> Unit
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.bannerImage)
        val title: TextView = itemView.findViewById(R.id.bannerTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val movie = movies[position]
        holder.title.text = movie.title
        
        // Use backdrop size for banners
        val backdropUrl = if (movie.posterPath != null) "https://image.tmdb.org/t/p/w780${movie.posterPath}" else ""
        holder.image.load(backdropUrl) { crossfade(true) }

        holder.itemView.setOnClickListener { onItemClick(movie) }
    }

    override fun getItemCount(): Int = movies.size
}