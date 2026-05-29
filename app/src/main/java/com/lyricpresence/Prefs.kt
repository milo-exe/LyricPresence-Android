package com.lyricpresence

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("lyricpresence", Context.MODE_PRIVATE)
    }

    var spotifyClientId: String
        get() = prefs.getString("spotifyClientId", "") ?: ""
        set(v) = prefs.edit().putString("spotifyClientId", v).apply()

    var spotifyAccessToken: String
        get() = prefs.getString("spotifyAccessToken", "") ?: ""
        set(v) = prefs.edit().putString("spotifyAccessToken", v).apply()

    var spotifyRefreshToken: String
        get() = prefs.getString("spotifyRefreshToken", "") ?: ""
        set(v) = prefs.edit().putString("spotifyRefreshToken", v).apply()

    var spotifyTokenExpiry: Long
        get() = prefs.getLong("spotifyTokenExpiry", 0L)
        set(v) = prefs.edit().putLong("spotifyTokenExpiry", v).apply()

    var discordToken: String
        get() = prefs.getString("discordToken", "") ?: ""
        set(v) = prefs.edit().putString("discordToken", v).apply()

    var statusPrefix: String
        get() = prefs.getString("statusPrefix", "♫") ?: "♫"
        set(v) = prefs.edit().putString("statusPrefix", v).apply()

    var lyricsCase: String
        get() = prefs.getString("lyricsCase", "lower") ?: "lower"
        set(v) = prefs.edit().putString("lyricsCase", v).apply()

    var idleTimeoutMinutes: Int
        get() = prefs.getInt("idleTimeoutMinutes", 5)
        set(v) = prefs.edit().putInt("idleTimeoutMinutes", v).apply()

    var hasOnboarded: Boolean
        get() = prefs.getBoolean("hasOnboarded", false)
        set(v) = prefs.edit().putBoolean("hasOnboarded", v).apply()

    var codeVerifier: String
        get() = prefs.getString("codeVerifier", "") ?: ""
        set(v) = prefs.edit().putString("codeVerifier", v).apply()
}
