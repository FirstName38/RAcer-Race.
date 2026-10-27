package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"
    private const val FALLBACK_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(
        prompt: String,
        apiKeyOverride: String? = null,
        endpointOverride: String? = null,
        bitmap: Bitmap? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val key = if (!apiKeyOverride.isNullOrBlank()) apiKeyOverride else BuildConfig.GEMINI_API_KEY
            if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(IllegalStateException("API key not configured"))
            }

            val requestJson = JSONObject()
            val contentsArr = JSONArray()
            val contentObj = JSONObject()
            val partsArr = JSONArray()

            val textPart = JSONObject().put("text", prompt)
            partsArr.put(textPart)

            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

                val imagePart = JSONObject().put(
                    "inlineData",
                    JSONObject()
                        .put("mimeType", "image/jpeg")
                        .put("data", base64Image)
                )
                partsArr.put(imagePart)
            }

            contentObj.put("parts", partsArr)
            contentsArr.put(contentObj)
            requestJson.put("contents", contentsArr)

            val primaryUrl = if (!endpointOverride.isNullOrBlank()) endpointOverride else "$BASE_URL?key=$key"
            val fallbackUrl = if (!endpointOverride.isNullOrBlank()) endpointOverride else "$FALLBACK_URL?key=$key"

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            var request = Request.Builder()
                .url(primaryUrl)
                .post(requestBody)
                .build()

            var response = client.newCall(request).execute()
            var responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful && endpointOverride.isNullOrBlank()) {
                // Try fallback model
                request = Request.Builder()
                    .url(fallbackUrl)
                    .post(requestJson.toString().toRequestBody(mediaType))
                    .build()
                response = client.newCall(request).execute()
                responseBody = response.body?.string() ?: ""
            }

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("HTTP ${response.code}: $responseBody")
                )
            }

            val parsedJson = JSONObject(responseBody)
            val candidates = parsedJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    return@withContext Result.success(text)
                }
            }

            Result.failure(Exception("Empty AI candidate response"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
