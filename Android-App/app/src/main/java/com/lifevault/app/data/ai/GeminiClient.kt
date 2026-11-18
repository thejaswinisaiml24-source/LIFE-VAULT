package com.lifevault.app.data.ai

import com.lifevault.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Minimal direct client for the Gemini API (generativelanguage.googleapis.com).
 * The API key is read from BuildConfig (populated from local.properties,
 * which is gitignored) rather than hardcoded in source.
 *
 * HONEST CAVEAT: this calls Gemini directly from the app, so the API key
 * ships inside the APK and could be extracted by a determined attacker. For
 * a hackathon/demo build that's an acceptable tradeoff; a production app
 * should proxy this through a backend (e.g. a Cloud Function) so the key
 * never leaves the server. Not implemented here — flagged, not hidden.
 */
object GeminiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    suspend fun sendMessage(conversation: List<Pair<String, String>>, systemContext: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("Gemini API key isn't configured. Add GEMINI_API_KEY to local.properties.")
                    )
                }

                val contents = JSONArray()

                val systemTurn = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", systemContext)))
                }
                contents.put(systemTurn)
                contents.put(
                    JSONObject().apply {
                        put("role", "model")
                        put("parts", JSONArray().put(JSONObject().put("text", "Understood, I'll help with that.")))
                    }
                )

                conversation.forEach { (role, text) ->
                    contents.put(
                        JSONObject().apply {
                            put("role", if (role == "user") "user" else "model")
                            put("parts", JSONArray().put(JSONObject().put("text", text)))
                        }
                    )
                }

                val body = JSONObject().apply {
                    put("contents", contents)
                }.toString()

                val request = Request.Builder()
                    .url("$ENDPOINT?key=$apiKey")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Gemini API error (${response.code}): $responseBody"))
                    }
                    val json = JSONObject(responseBody)
                    val text = json.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    Result.success(text)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
