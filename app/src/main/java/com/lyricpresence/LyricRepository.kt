package com.lyricpresence

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class TrackInfo(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Int,
    val progressMs: Int,
    val isPlaying: Boolean,
    val albumArtUrl: String?
)

object LyricRepository {
    val isRunning = MutableStateFlow(false)
    val currentTrack: MutableStateFlow<TrackInfo?> = MutableStateFlow(null)
    val nextTrack: MutableStateFlow<TrackInfo?> = MutableStateFlow(null)
    val currentLyric = MutableStateFlow("")
    val isPaused = MutableStateFlow(false)
    val errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)
    val isSpotifyAuthorized = MutableStateFlow(false)
}
