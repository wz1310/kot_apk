package com.wuodoo.mobile

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build

class App : Application() {

    companion object {
        lateinit var prefs: SharedPreferences
            private set

        const val KEY_SAVED_SERVERS = "saved_servers"
        const val KEY_LAST_SERVER   = "last_server"
        const val KEY_LAST_CRASH    = "last_crash"
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("wu_odoo_prefs", MODE_PRIVATE)
        setupCrashHandler()
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // Simpan stack trace ke SharedPreferences
                val sb = StringBuilder()
                sb.appendLine("=== CRASH REPORT ===")
                sb.appendLine("Time: ${java.util.Date()}")
                sb.appendLine("Thread: ${thread.name}")
                sb.appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                sb.appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                sb.appendLine("App: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                sb.appendLine()
                sb.appendLine("Exception: ${throwable.javaClass.name}")
                sb.appendLine("Message: ${throwable.message}")
                sb.appendLine()
                sb.appendLine("Stack Trace:")
                sb.appendLine(throwable.stackTraceToString())

                // Tambah caused by
                var cause = throwable.cause
                var depth = 0
                while (cause != null && depth < 5) {
                    sb.appendLine()
                    sb.appendLine("Caused by: ${cause.javaClass.name}: ${cause.message}")
                    sb.appendLine(cause.stackTraceToString())
                    cause = cause.cause
                    depth++
                }

                val crashLog = sb.toString()
                prefs.edit().putString(KEY_LAST_CRASH, crashLog).commit()

                // Buka CrashActivity untuk tampilkan error
                val intent = Intent(applicationContext, CrashActivity::class.java).apply {
                    putExtra("crash_log", crashLog)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)

            } catch (e: Exception) {
                // Jika crash handler sendiri crash, pakai default handler
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
