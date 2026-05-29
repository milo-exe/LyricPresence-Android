package com.lyricpresence

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object DiscordManager {
    private val client = OkHttpClient()

    suspend fun setStatus(text: String) {
        val prefix = Prefs.statusPrefix
        val full = if (prefix.isEmpty()) text else "$prefix $text"
        patch(JSONObject().put("custom_status", JSONObject().put("text", full)))
    }

    suspend fun clearStatus() {
        patch(JSONObject().put("custom_status", JSONObject.NULL))
    }

    private suspend fun patch(body: JSONObject) = withContext(Dispatchers.IO) {
        val token = Prefs.discordToken
        if (token.isEmpty()) return@withContext
        val req = Request.Builder()
            .url("https://discord.com/api/v9/users/@me/settings")
            .patch(body.toString().toRequestBody("application/json".toMediaType()))
            .header("Authorization", token)
            .header("Content-Type", "application/json")
            .build()
        runCatching { client.newCall(req).execute().close() }
    }
}
