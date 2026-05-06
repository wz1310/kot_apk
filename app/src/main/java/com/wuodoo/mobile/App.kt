package com.wuodoo.mobile

import android.app.Application
import android.content.SharedPreferences

class App : Application() {

    companion object {
        lateinit var prefs: SharedPreferences
            private set

        // Keys
        const val KEY_SAVED_SERVERS = "saved_servers"
        const val KEY_LAST_SERVER   = "last_server"
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("wu_odoo_prefs", MODE_PRIVATE)
    }
}
