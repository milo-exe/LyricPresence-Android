package com.lyricpresence.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lyricpresence.Prefs
import com.lyricpresence.SpotifyManager
import com.lyricpresence.LyricRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val isAuthorized by LyricRepository.isSpotifyAuthorized.collectAsState()

    var clientId by remember { mutableStateOf(Prefs.spotifyClientId) }
    var discordToken by remember { mutableStateOf(Prefs.discordToken) }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (page) {
                    0 -> {
                        Text("♫", fontSize = 72.sp)
                        Spacer(Modifier.height(20.dp))
                        Text("LyricPresence", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Show your Spotify lyrics\nas your Discord status — live.",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(40.dp))
                        OnboardingButton("Get Started") { scope.launch { pagerState.animateScrollToPage(1) } }
                    }
                    1 -> {
                        Text("🎵", fontSize = 56.sp)
                        Spacer(Modifier.height(20.dp))
                        Text("Connect Spotify", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Enter your Spotify Client ID.\nGet one free at developer.spotify.com",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        OutlinedTextField(
                            value = clientId,
                            onValueChange = { clientId = it; Prefs.spotifyClientId = it },
                            label = { Text("Client ID") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = textFieldColors()
                        )
                        Spacer(Modifier.height(12.dp))
                        if (isAuthorized) {
                            Text("✓ Connected!", color = Color.Green, fontWeight = FontWeight.SemiBold)
                        } else {
                            Button(
                                onClick = { SpotifyManager.authorize(context) },
                                enabled = clientId.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Connect Spotify", color = Color.Black, fontWeight = FontWeight.SemiBold) }
                        }
                        Spacer(Modifier.height(24.dp))
                        OnboardingButton("Next →") { scope.launch { pagerState.animateScrollToPage(2) } }
                    }
                    2 -> {
                        Text("💬", fontSize = 56.sp)
                        Spacer(Modifier.height(20.dp))
                        Text("Discord Token", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Open Discord in your browser → DevTools → Network → any request → Authorization header.",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        OutlinedTextField(
                            value = discordToken,
                            onValueChange = { discordToken = it; Prefs.discordToken = it },
                            label = { Text("Paste token here") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = textFieldColors()
                        )
                        Spacer(Modifier.height(24.dp))
                        OnboardingButton("Done") {
                            Prefs.hasOnboarded = true
                            onDone()
                        }
                    }
                }
            }
        }

        // Page dots
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { i ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == i) 10.dp else 6.dp)
                        .let {
                            it
                        }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(50),
                        color = if (pagerState.currentPage == i) Color.White else Color.White.copy(alpha = 0.3f)
                    ) {}
                }
            }
        }
    }
}

@Composable
fun OnboardingButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(200.dp).height(48.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
    ) {
        Text(text, color = Color.Black, fontWeight = FontWeight.SemiBold)
    }
}
