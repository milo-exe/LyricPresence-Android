package com.lyricpresence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.*

class LyricForegroundService : Service() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var pollJob: Job? = null
    private var lyricJob: Job? = null
    private var lastTrackId: String? = null
    private var lyrics: List<LyricLine> = emptyList()
    private var lastFetchTime: Long = 0L
    private var lastProgressMs: Int = 0
    private var lastPlayedAt: Long = 0L

    private val estimatedProgressMs: Int
        get() = lastProgressMs + ((System.currentTimeMillis() - lastFetchTime)).toInt()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText("Running...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setSilent(true)
            .build()
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        LyricRepository.isRunning.value = true
        startPolling()
        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        LyricRepository.isRunning.value = false
        LyricRepository.currentLyric.value = ""
        LyricRepository.isPaused.value = false
        scope.launch { DiscordManager.clearStatus() }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startPolling() {
        pollJob = scope.launch {
            while (isActive) {
                fetchTrack()
                delay(3000)
            }
        }
    }

    private suspend fun fetchTrack() {
        val track = SpotifyManager.fetchCurrentTrack()
        if (track == null) {
            LyricRepository.errorMessage.value = "Could not reach Spotify."
            if (LyricRepository.currentTrack.value != null) {
                LyricRepository.currentTrack.value = null
                LyricRepository.nextTrack.value = null
                LyricRepository.currentLyric.value = ""
                lyricJob?.cancel()
                DiscordManager.clearStatus()
            }
            return
        }

        LyricRepository.errorMessage.value = null
        LyricRepository.currentTrack.value = track

        if (track.isPlaying) lastPlayedAt = System.currentTimeMillis()
        else if (System.currentTimeMillis() - lastPlayedAt > Prefs.idleTimeoutMinutes * 60_000L) {
            stopSelf()
            return
        }

        if (!track.isPlaying) {
            if (!LyricRepository.isPaused.value) {
                LyricRepository.isPaused.value = true
                LyricRepository.currentLyric.value = ""
                lyricJob?.cancel()
                DiscordManager.clearStatus()
            }
            return
        }

        if (LyricRepository.isPaused.value) LyricRepository.isPaused.value = false

        lastFetchTime = System.currentTimeMillis()
        lastProgressMs = track.progressMs

        if (track.id != lastTrackId) {
            lastTrackId = track.id
            lyrics = LyricsManager.fetch(track)
            LyricRepository.nextTrack.value = SpotifyManager.fetchNextInQueue()
            lyricJob?.cancel()
            scheduleLyrics()
        }
    }

    private fun scheduleLyrics() {
        lyricJob = scope.launch {
            while (isActive) {
                val progress = estimatedProgressMs
                val line = LyricsManager.currentLine(lyrics, progress)
                if (line != null && line != LyricRepository.currentLyric.value) {
                    LyricRepository.currentLyric.value = line
                    DiscordManager.setStatus(applyCase(line))
                    updateNotification(line)
                } else if (lyrics.isEmpty() && LyricRepository.currentLyric.value.isNotEmpty()) {
                    LyricRepository.currentLyric.value = ""
                    DiscordManager.clearStatus()
                }

                val next = LyricsManager.nextLine(lyrics, estimatedProgressMs)
                if (next != null) {
                    val delayMs = (next.timestampMs - estimatedProgressMs).toLong().coerceAtLeast(50)
                    delay(delayMs)
                } else {
                    delay(3000)
                    break
                }
            }
        }
    }

    private fun applyCase(text: String) = when (Prefs.lyricsCase) {
        "upper" -> text.uppercase()
        "original" -> text
        else -> text.lowercase()
    }

    private fun updateNotification(lyric: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(LyricRepository.currentTrack.value?.title ?: getString(R.string.notification_title))
            .setContentText(lyric)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setSilent(true)
            .build()
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "lyricpresence"
        const val NOTIFICATION_ID = 1
    }
}
