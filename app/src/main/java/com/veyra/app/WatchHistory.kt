package com.veyra.app

import android.content.Context
import android.content.SharedPreferences

object WatchHistory {
    private const val PREF_NAME = "veyra_history"
    private const val KEY_HISTORY = "watched_ids"
    private const val KEY_TITLES = "watched_titles"
    private const val KEY_POSTERS = "watched_posters"
    private const val KEY_IS_TV = "watched_istv"

    fun addToHistory(context: Context, movie: Movie) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        var ids = prefs.getString(KEY_HISTORY, "") ?: ""
        var titles = prefs.getString(KEY_TITLES, "") ?: ""
        var posters = prefs.getString(KEY_POSTERS, "") ?: ""
        var isTvs = prefs.getString(KEY_IS_TV, "") ?: ""

        // Simple prepending (newest first)
        ids = "${movie.id},$ids"
        titles = "${movie.title.replace(",", ";")},$titles"
        posters = "${movie.posterPath},$posters"
        isTvs = "${movie.isTvShow},$isTvs"

        prefs.edit()
            .putString(KEY_HISTORY, ids)
            .putString(KEY_TITLES, titles)
            .putString(KEY_POSTERS, posters)
            .putString(KEY_IS_TV, isTvs)
            .apply()
    }

    fun getHistory(context: Context): List<Movie> {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val ids = prefs.getString(KEY_HISTORY, "")?.split(",")?.filter { it.isNotEmpty() } ?: return emptyList()
        val titles = prefs.getString(KEY_TITLES, "")?.split(",")?.map { it.replace(";", ",") } ?: emptyList()
        val posters = prefs.getString(KEY_POSTERS, "")?.split(",") ?: emptyList()
        val isTvs = prefs.getString(KEY_IS_TV, "")?.split(",") ?: emptyList()

        val history = mutableListOf<Movie>()
        for (i in ids.indices) {
            if (i < titles.size && i < posters.size && i < isTvs.size) {
                history.add(Movie(
                    id = ids[i].toIntOrNull() ?: 0,
                    title = titles[i],
                    posterPath = if (posters[i] == "null") null else posters[i],
                    isTvShow = isTvs[i] == "true"
                ))
            }
        }
        return history
    }
}