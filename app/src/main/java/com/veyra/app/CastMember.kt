package com.veyra.app

data class CastMember(
    val name: String,
    val character: String,
    val profilePath: String?
) {
    fun getProfileUrl(): String {
        return if (profilePath != null) "https://image.tmdb.org/t/p/w185$profilePath" else ""
    }
}