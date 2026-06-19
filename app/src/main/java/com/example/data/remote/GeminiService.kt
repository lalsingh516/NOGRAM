package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Calls Gemini-3.5-flash API via direct HTTP POST
     * Injected token: BuildConfig.GEMINI_API_KEY
     */
    suspend fun getAiResponse(userPrompt: String, systemInstruction: String = "You are NOGRAM Assistant, a helpful AI embedded inside the secure NOGRAM messaging app."): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured or placeholder.")
            return@withContext "API Configuration Error: Please enter your Gemini API Key inside the Secrets panel of AI Studio to enable NOGRAM AI features."
        }

        try {
            // Build the body JSON manually using JSONObject to guarantee perfect compilation with no extra dependencies
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userPrompt)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Unsuccessful Gemini API request: ${response.code} body: $errBody")
                    return@withContext "AI Server Error (${response.code}). Please verify your API keys and parameters."
                }

                val responseString = response.body?.string()
                if (responseString.isNullOrEmpty()) {
                    return@withContext "Empty response received from the NOGRAM AI brain."
                }

                // Parse response utilizing JSONObject
                val jsonObject = JSONObject(responseString)
                val candidates = jsonObject.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    if (contentObj != null) {
                        val parts = contentObj.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No text part found.")
                        }
                    }
                }
                "We apologized, but the AI could not structuralize its response."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getAiResponse", e)
            "Error: ${e.localizedMessage ?: "Failed connection to secure NOGRAM server."}"
        }
    }
}
