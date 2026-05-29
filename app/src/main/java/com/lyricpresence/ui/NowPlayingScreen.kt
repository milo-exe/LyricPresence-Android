package com.lyricpresence.ui

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lyricpresence.LyricForegroundService
import com.lyricpresence.LyricRepository

@Composable
fun NowPlayingScreen() {
    val context = LocalContext.current
    val isRunning by LyricRepository.isRunning.collectAsState()
    val track by LyricRepository.currentTrack.collectAsState()
    val nextTrack by LyricRepository.nextTrack.collectAsState()
    val currentLyric by LyricRepository.currentLyric.collectAsState()
    val isPaused by LyricRepository.isPaused.collectAsState()
    val errorMessage by LyricRepository.errorMessage.collectAsState()
    val isAuthorized by LyricRepository.isSpotifyAuthorized.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Blurred background
        AsyncImage(
            model = track?.albumArtUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(60.dp),
            contentScale = ContentScale.Crop
        )
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.55f)
        ) {}

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            // Album art
            AsyncImage(
                model = track?.albumArtUrl,
                contentDescription = "Album art",
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(28.dp))

            // Song info
            if (track != null) {
                Text(
                    text = track!!.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = track!!.artist,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isPaused) {
                    Spacer(Modifier.height(2.dp))
                    Text("paused", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                }
            } else {
                Text(
                    text = if (isRunning) "Nothing playing" else "Tap start to begin",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Error
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    fontSize = 12.sp,
                    color = Color.Red.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
            }

            // Lyric rectangle
            AnimatedContent(targetState = currentLyric, label = "lyric") { lyric ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    tonalElevation = 0.dp
                ) {
                    Text(
                        text = lyric.ifEmpty { "♫" },
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        fontSize = 15.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 3
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Next up
            if (nextTrack != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = nextTrack!!.albumArtUrl,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Next up", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                        Text(
                            "${nextTrack!!.title} — ${nextTrack!!.artist}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Start / Stop
            Button(
                onClick = {
                    if (isRunning) {
                        context.stopService(Intent(context, LyricForegroundService::class.java))
                    } else {
                        context.startForegroundService(Intent(context, LyricForegroundService::class.java))
                    }
                },
                enabled = isAuthorized,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color.Red.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.width(140.dp).height(50.dp)
            ) {
                Text(
                    text = if (isRunning) "Stop" else "Start",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(12.dp))
            Text("made by @kikq on discord", fontSize = 10.sp, color = Color.White.copy(alpha = 0.25f))
            Spacer(Modifier.weight(1f))
        }
    }
}
