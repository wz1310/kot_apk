package com.wuodoo.mobile.data

import com.wuodoo.mobile.App
import org.json.JSONArray
import org.json.JSONObject

object ServerRepository {

    fun getSavedServers(): MutableList<ServerInfo> {
        val raw = App.prefs.getString(App.KEY_SAVED_SERVERS, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { ServerInfo.fromJson(arr.getJSONObject(it)) }.toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun saveServer(server: ServerInfo) {
        val list = getSavedServers()
        // Hapus duplikat berdasarkan host+protocol
        list.removeAll { it.host == server.host && it.protocol == server.protocol }
        list.add(0, server)
        if (list.size > 5) list.subList(5, list.size).clear()
        val arr = JSONArray().apply { list.forEach { put(it.toJson()) } }
        App.prefs.edit().putString(App.KEY_SAVED_SERVERS, arr.toString()).apply()
    }

    fun deleteServer(index: Int) {
        val list = getSavedServers()
        if (index in list.indices) {
            list.removeAt(index)
            val arr = JSONArray().apply { list.forEach { put(it.toJson()) } }
            App.prefs.edit().putString(App.KEY_SAVED_SERVERS, arr.toString()).apply()
        }
    }

    fun getLastServer(): ServerInfo? {
        val raw = App.prefs.getString(App.KEY_LAST_SERVER, null) ?: return null
        return try { ServerInfo.fromJson(JSONObject(raw)) } catch (e: Exception) { null }
    }

    fun saveLastServer(server: ServerInfo) {
        App.prefs.edit().putString(App.KEY_LAST_SERVER, server.toJson().toString()).apply()
    }
}
