package com.wuodoo.mobile.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.wuodoo.mobile.data.ServerRepository

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Delay singkat untuk splash, lalu cek apakah ada server tersimpan
        Handler(Looper.getMainLooper()).postDelayed({
            val last = ServerRepository.getLastServer()
            if (last != null && last.database.isNotEmpty()) {
                // Langsung buka Odoo dengan server terakhir
                startActivity(OdooActivity.newIntent(this, last))
            } else {
                // Buka halaman connect
                startActivity(Intent(this, ConnectActivity::class.java))
            }
            finish()
        }, 1200)
    }
}
