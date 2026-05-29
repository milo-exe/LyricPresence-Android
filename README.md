# LyricPresence Android

Displays your Spotify lyrics in real-time on your Discord custom status — running fully in the background on Android.

A mobile port of [LyricPresence](https://github.com/milo-exe/LyricPresence) by milo-exe.

---

## How it Works

- Reads your current Spotify playback via the Spotify Web API
- Fetches time-synced lyrics from [LRCLIB](https://lrclib.net)
- Updates your Discord custom status line-by-line as the song plays
- Stays alive in the background using an Android Foreground Service

---

## Requirements

- Android 12 or later
- Spotify account
- Discord account
- No signing service needed — just enable "Install from unknown sources"

---

## Setup

**1. Get a Spotify Client ID**

Go to [developer.spotify.com](https://developer.spotify.com), create an app, and add the following redirect URI:

```
lyricpresence://callback
```

Copy your **Client ID**.

**2. Get your Discord token**

Open Discord in your browser → open DevTools (F12) → go to **Network** → send any message → find a request with an `Authorization` header. Copy that value.

> ⚠️ Never share your Discord token. Anyone with it has full access to your account.

**3. Install the APK**

Download the latest APK from the [Releases](../../releases) tab, then:

1. On your Android device go to **Settings → Apps → Special app access → Install unknown apps**
2. Allow your browser or file manager to install APKs
3. Open the downloaded APK and install it

**4. Configure the app**

Open LyricPresence — the onboarding screen will guide you through:
- Entering your Spotify Client ID and connecting your account
- Pasting your Discord token

Then go to **Now Playing** and tap **Start**.

---

## Notes

- Lyrics are sourced from LRCLIB. If no synced lyrics are found for a song, the Discord status is cleared silently.
- The app runs as a Foreground Service — you will see a persistent notification while it's active. This is required by Android for background apps.
- Updating Discord status with a user token is against Discord's ToS. Use at your own risk.

---

## Troubleshooting

**Status stops updating**
Make sure the app is not being killed by battery optimization. Go to **Settings → Battery → LyricPresence** and set it to "Unrestricted".

**"Nothing playing" even though Spotify is running**
Make sure Spotify is actively playing (not paused) and that you completed the Spotify login during onboarding.

**Discord status not changing**
Double-check your Discord token — tokens expire if you change your password or log out of all devices.
