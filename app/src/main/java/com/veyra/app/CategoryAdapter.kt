package com.veyra.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categories: List<MovieCategory>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.categoryTitle)
        val horizontalRecyclerView: RecyclerView = itemView.findViewById(R.id.categoryRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.titleText.text = category.title

        // Setup the horizontal list inside this category
        holder.horizontalRecyclerView.layoutManager = LinearLayoutManager(
            holder.itemView.context, 
            LinearLayoutManager.HORIZONTAL, 
            false
        )
        holder.horizontalRecyclerView.adapter = MoviePosterAdapter(category.movies, onMovieClick)
        
        // Prevent scrolling conflicts on low-end devices
        holder.horizontalRecyclerView.isNestedScrollingEnabled = false 
    }

    override fun getItemCount(): Int = categories.size
}