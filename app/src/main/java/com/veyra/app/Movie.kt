package com.veyra.app

data class Movie(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val isTvShow: Boolean = false
) {
    fun getFullPosterUrl(): String {
        return if (posterPath != null) "https://image.tmdb.org/t/p/w500$posterPath" else ""
    }
}