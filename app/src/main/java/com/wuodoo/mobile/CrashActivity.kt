package com.wuodoo.mobile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wuodoo.mobile.databinding.ActivityCrashBinding
import com.wuodoo.mobile.ui.ConnectActivity

class CrashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil crash log dari intent atau SharedPreferences
        val crashLog = intent.getStringExtra("crash_log")
            ?: App.prefs.getString(App.KEY_LAST_CRASH, "Tidak ada log crash.")
            ?: "Tidak ada log crash."

        binding.tvCrashLog.text = crashLog

        // Tombol Copy
        binding.btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Crash Log", crashLog))
            Toast.makeText(this, "Log disalin ke clipboard", Toast.LENGTH_SHORT).show()
        }

        // Tombol Restart
        binding.btnRestart.setOnClickListener {
            App.prefs.edit().remove(App.KEY_LAST_CRASH).apply()
            startActivity(Intent(this, ConnectActivity::class.java))
            finish()
        }

        // Tombol Clear & Restart
        binding.btnClear.setOnClickListener {
            App.prefs.edit().clear().apply()
            startActivity(Intent(this, ConnectActivity::class.java))
            finish()
        }
    }
}
