package com.lyricpresence

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

data class LyricLine(val timestampMs: Int, val text: String)

object LyricsManager {
    private val client = OkHttpClient()
    private var cachedTrackId: String? = null
    private var cachedLines: List<LyricLine> = emptyList()

    suspend fun fetch(track: TrackInfo): List<LyricLine> = withContext(Dispatchers.IO) {
        if (track.id == cachedTrackId) return@withContext cachedLines
        val artist = URLEncoder.encode(track.artist, "UTF-8")
        val title = URLEncoder.encode(track.title, "UTF-8")
        val album = URLEncoder.encode(track.album, "UTF-8")
        val duration = track.durationMs / 1000
        val url = "https://lrclib.net/api/get?artist_name=$artist&track_name=$title&album_name=$album&duration=$duration"
        val req = Request.Builder().url(url).build()
        val resp = runCatching { client.newCall(req).execute() }.getOrNull() ?: return@withContext emptyList()
        if (!resp.isSuccessful) return@withContext emptyList()
        val json = runCatching { JSONObject(resp.body!!.string()) }.getOrNull() ?: return@withContext emptyList()
        val synced = json.optString("syncedLyrics", "")
        if (synced.isEmpty()) return@withContext emptyList()
        val lines = parseLrc(synced)
        cachedTrackId = track.id
        cachedLines = lines
        lines
    }

    fun currentLine(lines: List<LyricLine>, progressMs: Int): String? {
        var result: LyricLine? = null
        for (line in lines) {
            if (line.timestampMs <= progressMs) result = line else break
        }
        return if (result?.text?.isNotBlank() == true) result.text else null
    }

    fun nextLine(lines: List<LyricLine>, progressMs: Int): LyricLine? =
        lines.firstOrNull { it.timestampMs > progressMs }

    private fun parseLrc(lrc: String): List<LyricLine> {
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")
        return lrc.lines().mapNotNull { line ->
            val match = regex.find(line) ?: return@mapNotNull null
            val min = match.groupValues[1].toInt()
            val sec = match.groupValues[2].toInt()
            val msStr = match.groupValues[3]
            val ms = if (msStr.length == 2) msStr.toInt() * 10 else msStr.toInt()
            val text = match.groupValues[4].trim()
            LyricLine((min * 60 + sec) * 1000 + ms, text)
        }.sortedBy { it.timestampMs }
    }
}
