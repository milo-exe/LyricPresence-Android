package com.lyricpresence

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

object SpotifyManager {
    private val client = OkHttpClient()
    private const val REDIRECT_URI = "lyricpresence://callback"
    private const val SCOPES = "user-read-currently-playing user-read-playback-state"

    fun authorize(context: Context) {
        val verifier = generateVerifier()
        Prefs.codeVerifier = verifier
        val challenge = generateChallenge(verifier)

        val url = Uri.parse("https://accounts.spotify.com/authorize").buildUpon()
            .appendQueryParameter("client_id", Prefs.spotifyClientId)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("scope", SCOPES)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", challenge)
            .build()

        CustomTabsIntent.Builder().build().launchUrl(context, url)
    }

    suspend fun handleCallback(code: String) = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", REDIRECT_URI)
            .add("client_id", Prefs.spotifyClientId)
            .add("code_verifier", Prefs.codeVerifier)
            .build()
        val req = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .post(body)
            .build()
        val resp = client.newCall(req).execute()
        val json = JSONObject(resp.body!!.string())
        saveTokens(json)
        LyricRepository.isSpotifyAuthorized.value = true
    }

    suspend fun fetchCurrentTrack(): TrackInfo? = withContext(Dispatchers.IO) {
        if (!ensureValidToken()) return@withContext null
        val req = Request.Builder()
            .url("https://api.spotify.com/v1/me/player/currently-playing")
            .header("Authorization", "Bearer ${Prefs.spotifyAccessToken}")
            .build()
        val resp = runCatching { client.newCall(req).execute() }.getOrNull() ?: return@withContext null
        if (resp.code != 200) return@withContext null
        val json = JSONObject(resp.body!!.string())
        parseTrack(json)
    }

    suspend fun fetchNextInQueue(): TrackInfo? = withContext(Dispatchers.IO) {
        if (!ensureValidToken()) return@withContext null
        val req = Request.Builder()
            .url("https://api.spotify.com/v1/me/player/queue")
            .header("Authorization", "Bearer ${Prefs.spotifyAccessToken}")
            .build()
        val resp = runCatching { client.newCall(req).execute() }.getOrNull() ?: return@withContext null
        if (!resp.isSuccessful) return@withContext null
        val json = JSONObject(resp.body!!.string())
        val queue = json.optJSONArray("queue") ?: return@withContext null
        if (queue.length() == 0) return@withContext null
        parseItem(queue.getJSONObject(0), 0, false)
    }

    fun logout() {
        Prefs.spotifyAccessToken = ""
        Prefs.spotifyRefreshToken = ""
        Prefs.spotifyTokenExpiry = 0L
        LyricRepository.isSpotifyAuthorized.value = false
    }

    fun checkAuthorized() {
        LyricRepository.isSpotifyAuthorized.value = Prefs.spotifyRefreshToken.isNotEmpty()
    }

    private fun parseTrack(json: JSONObject): TrackInfo? {
        return runCatching {
            val item = json.getJSONObject("item")
            val progressMs = json.getInt("progress_ms")
            val isPlaying = json.getBoolean("is_playing")
            parseItem(item, progressMs, isPlaying)
        }.getOrNull()
    }

    private fun parseItem(item: JSONObject, progressMs: Int, isPlaying: Boolean): TrackInfo {
        val id = item.getString("id")
        val name = item.getString("name")
        val artist = item.getJSONArray("artists").getJSONObject(0).getString("name")
        val albumObj = item.getJSONObject("album")
        val album = albumObj.getString("name")
        val duration = item.getInt("duration_ms")
        val images = albumObj.optJSONArray("images")
        val artUrl = if (images != null && images.length() > 0) images.getJSONObject(0).getString("url") else null
        return TrackInfo(id, name, artist, album, duration, progressMs, isPlaying, artUrl)
    }

    private suspend fun ensureValidToken(): Boolean {
        if (Prefs.spotifyAccessToken.isNotEmpty() && System.currentTimeMillis() < Prefs.spotifyTokenExpiry) return true
        if (Prefs.spotifyRefreshToken.isEmpty()) return false
        return withContext(Dispatchers.IO) {
            val body = FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", Prefs.spotifyRefreshToken)
                .add("client_id", Prefs.spotifyClientId)
                .build()
            val req = Request.Builder().url("https://accounts.spotify.com/api/token").post(body).build()
            val resp = runCatching { client.newCall(req).execute() }.getOrNull() ?: return@withContext false
            if (!resp.isSuccessful) return@withContext false
            saveTokens(JSONObject(resp.body!!.string()))
            true
        }
    }

    private fun saveTokens(json: JSONObject) {
        Prefs.spotifyAccessToken = json.optString("access_token", "")
        val expiresIn = json.optInt("expires_in", 3600)
        Prefs.spotifyTokenExpiry = System.currentTimeMillis() + (expiresIn - 60) * 1000L
        val refresh = json.optString("refresh_token", "")
        if (refresh.isNotEmpty()) Prefs.spotifyRefreshToken = refresh
    }

    private fun generateVerifier(): String {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun generateChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray())
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
}
