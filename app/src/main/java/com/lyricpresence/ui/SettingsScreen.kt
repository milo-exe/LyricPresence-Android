package com.lyricpresence.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lyricpresence.LyricRepository
import com.lyricpresence.Prefs
import com.lyricpresence.SpotifyManager

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val isAuthorized by LyricRepository.isSpotifyAuthorized.collectAsState()

    var clientId by remember { mutableStateOf(Prefs.spotifyClientId) }
    var discordToken by remember { mutableStateOf(Prefs.discordToken) }
    var statusPrefix by remember { mutableStateOf(Prefs.statusPrefix) }
    var lyricsCase by remember { mutableStateOf(Prefs.lyricsCase) }
    var idleTimeout by remember { mutableIntStateOf(Prefs.idleTimeoutMinutes) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Settings", fontSize = 28.sp, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))

        // Spotify
        SettingsCard(title = "Spotify") {
            OutlinedTextField(
                value = clientId,
                onValueChange = { clientId = it; Prefs.spotifyClientId = it },
                label = { Text("Client ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            if (isAuthorized) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Connected", color = Color.White, modifier = Modifier.weight(1f))
                    Text("✓", color = Color.Green, fontSize = 18.sp)
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { SpotifyManager.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))
                ) { Text("Log out") }
            } else {
                Button(
                    onClick = { SpotifyManager.authorize(context) },
                    enabled = clientId.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) { Text("Connect Spotify", color = Color.Black) }
            }
        }

        // Discord
        SettingsCard(title = "Discord") {
            OutlinedTextField(
                value = discordToken,
                onValueChange = { discordToken = it; Prefs.discordToken = it },
                label = { Text("User Token") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "DevTools → Network → any request → Authorization header.",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }

        // Status Appearance
        SettingsCard(title = "Status Appearance") {
            OutlinedTextField(
                value = statusPrefix,
                onValueChange = { statusPrefix = it; Prefs.statusPrefix = it },
                label = { Text("Prefix") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors()
            )
            Spacer(Modifier.height(12.dp))
            Text("Lyrics Case", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("lower" to "lowercase", "original" to "Original", "upper" to "UPPER").forEachIndexed { i, (key, label) ->
                    SegmentedButton(
                        selected = lyricsCase == key,
                        onClick = { lyricsCase = key; Prefs.lyricsCase = key },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = 3),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Color.White.copy(alpha = 0.15f),
                            activeContentColor = Color.White,
                            inactiveContainerColor = Color.Transparent,
                            inactiveContentColor = Color.White.copy(alpha = 0.5f)
                        )
                    ) { Text(label, fontSize = 12.sp) }
                }
            }
        }

        // Behaviour
        SettingsCard(title = "Behaviour") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Idle timeout: ${idleTimeout}m", color = Color.White, modifier = Modifier.weight(1f))
                IconButton(onClick = { if (idleTimeout > 1) { idleTimeout--; Prefs.idleTimeoutMinutes = idleTimeout } }) {
                    Text("−", color = Color.White, fontSize = 20.sp)
                }
                IconButton(onClick = { if (idleTimeout < 60) { idleTimeout++; Prefs.idleTimeoutMinutes = idleTimeout } }) {
                    Text("+", color = Color.White, fontSize = 20.sp)
                }
            }
            Text(
                "Auto-stops if nothing plays for ${idleTimeout} minutes.",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.medium, color = Color.White.copy(alpha = 0.08f)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 12.dp))
            content()
        }
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color.White.copy(alpha = 0.5f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    focusedLabelColor = Color.White.copy(alpha = 0.7f),
    unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
    cursorColor = Color.White
)
