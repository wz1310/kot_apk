package com.wuodoo.mobile.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object OdooApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    /**
     * Ambil daftar database dari server Odoo.
     * Return list nama database, atau null jika error.
     */
    fun fetchDatabases(baseUrl: String): Result<List<String>> {
        return try {
            val body = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("method", "call")
                put("params", JSONObject())
            }.toString().toRequestBody(JSON_MEDIA)

            val request = Request.Builder()
                .url("$baseUrl/web/database/list")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val result = json.optJSONArray("result")
                if (result != null) {
                    val dbs = (0 until result.length()).map { result.getString(it) }
                    Result.success(dbs)
                } else {
                    // Server mungkin return error atau tidak support list
                    Result.success(emptyList())
                }
            } else {
                Result.success(emptyList()) // Fallback ke manual input
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
