package com.lyricpresence

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.lyricpresence.ui.*
import com.lyricpresence.ui.theme.LyricPresenceTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)
        SpotifyManager.checkAuthorized()
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        handleIntent(intent)

        setContent {
            LyricPresenceTheme {
                AppContent()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "lyricpresence" && data.host == "callback") {
            val code = data.getQueryParameter("code") ?: return
            kotlinx.coroutines.MainScope().launch {
                SpotifyManager.handleCallback(code)
            }
        }
    }
}

@Composable
fun AppContent() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val showOnboarding = remember { mutableStateOf(!Prefs.hasOnboarded) }

    if (showOnboarding.value) {
        OnboardingScreen(onDone = { showOnboarding.value = false })
        return
    }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            NavigationBar(containerColor = Color.Black.copy(alpha = 0.8f)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("♫", color = Color.White) },
                    label = { Text("Now Playing", color = Color.White.copy(alpha = 0.7f)) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.White.copy(alpha = 0.15f))
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("⚙", color = Color.White) },
                    label = { Text("Settings", color = Color.White.copy(alpha = 0.7f)) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.White.copy(alpha = 0.15f))
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> NowPlayingScreen()
            1 -> SettingsScreen()
        }
    }
}
