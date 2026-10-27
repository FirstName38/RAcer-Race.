package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object OpenAiApiClient {
    private const val OPENAI_URL = "https://api.openai.com/v1/chat/completions"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(
        prompt: String,
        apiKey: String,
        model: String = "gpt-4o-mini"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(IllegalStateException("OpenAI API key is blank"))
            }

            val requestJson = JSONObject()
            requestJson.put("model", model.ifBlank { "gpt-4o-mini" })

            val messagesArr = JSONArray()
            val systemMsg = JSONObject()
                .put("role", "system")
                .put("content", "You are Luma, a brilliant, empathetic, and knowledgeable academic & focus companion in FocusFlow. Answer questions with structured clarity, high intelligence, and motivating tone.")
            val userMsg = JSONObject()
                .put("role", "user")
                .put("content", prompt)

            messagesArr.put(systemMsg)
            messagesArr.put(userMsg)
            requestJson.put("messages", messagesArr)
            requestJson.put("temperature", 0.7)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(OPENAI_URL)
                .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                } catch (_: Exception) {
                    responseBody
                }
                return@withContext Result.failure(Exception("OpenAI Error ($errorMsg)"))
            }

            val parsedJson = JSONObject(responseBody)
            val choices = parsedJson.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                val messageObj = firstChoice.optJSONObject("message")
                val text = messageObj?.optString("content", "") ?: ""
                if (text.isNotBlank()) {
                    return@withContext Result.success(text.trim())
                }
            }

            Result.failure(Exception("Empty response from OpenAI"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
