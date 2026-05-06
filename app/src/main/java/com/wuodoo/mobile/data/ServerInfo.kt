package com.wuodoo.mobile.data

import org.json.JSONObject

data class ServerInfo(
    val host: String,
    val protocol: String = "http",
    val database: String = "",
    val ts: Long = System.currentTimeMillis()
) {
    val baseUrl: String get() = "$protocol://$host"
    val odooUrl: String get() = "$baseUrl/web?db=${database}"

    fun toJson(): JSONObject = JSONObject().apply {
        put("host", host)
        put("protocol", protocol)
        put("database", database)
        put("ts", ts)
    }

    companion object {
        fun fromJson(json: JSONObject) = ServerInfo(
            host     = json.optString("host"),
            protocol = json.optString("protocol", "http"),
            database = json.optString("database"),
            ts       = json.optLong("ts", System.currentTimeMillis())
        )
    }
}
