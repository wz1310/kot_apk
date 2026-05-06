package com.wuodoo.mobile.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.wuodoo.mobile.data.ServerInfo
import com.wuodoo.mobile.data.ServerRepository
import com.wuodoo.mobile.databinding.ActivityDatabaseBinding
import com.wuodoo.mobile.ui.adapter.DatabaseAdapter

class DatabaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatabaseBinding

    companion object {
        private const val EXTRA_HOST      = "host"
        private const val EXTRA_PROTOCOL  = "protocol"
        private const val EXTRA_DATABASES = "databases"

        fun newIntent(context: Context, host: String, protocol: String, databases: List<String>): Intent {
            return Intent(context, DatabaseActivity::class.java).apply {
                putExtra(EXTRA_HOST, host)
                putExtra(EXTRA_PROTOCOL, protocol)
                putStringArrayListExtra(EXTRA_DATABASES, ArrayList(databases))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatabaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val host      = intent.getStringExtra(EXTRA_HOST) ?: ""
        val protocol  = intent.getStringExtra(EXTRA_PROTOCOL) ?: "http"
        val databases = intent.getStringArrayListExtra(EXTRA_DATABASES) ?: arrayListOf()
        val baseUrl   = "$protocol://$host"

        binding.dbServerLabel.text = baseUrl
        binding.btnBackToConnect.setOnClickListener { finish() }

        if (databases.isEmpty()) {
            // Tampilkan manual input
            binding.dbList.visibility   = View.GONE
            binding.dbManual.visibility = View.VISIBLE
            binding.dbManualInfo.text   = "Daftar database tidak dapat diambil otomatis. Masukkan nama database secara manual."
        } else {
            binding.dbList.visibility   = View.VISIBLE
            binding.dbManual.visibility = View.GONE
            binding.rvDatabases.layoutManager = LinearLayoutManager(this)
            binding.rvDatabases.adapter = DatabaseAdapter(databases) { dbName ->
                openOdoo(host, protocol, dbName)
            }
            // Tombol untuk switch ke manual input
            binding.btnManualInput.setOnClickListener {
                binding.dbList.visibility   = View.GONE
                binding.dbManual.visibility = View.VISIBLE
            }
        }

        binding.btnDbManualConnect.setOnClickListener {
            val dbName = binding.inputDbname.text.toString().trim()
            if (dbName.isEmpty()) {
                binding.dbError.text = "Masukkan nama database."
                binding.dbError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            openOdoo(host, protocol, dbName)
        }
    }

    private fun openOdoo(host: String, protocol: String, database: String) {
        val server = ServerInfo(host = host, protocol = protocol, database = database)
        ServerRepository.saveServer(server)
        ServerRepository.saveLastServer(server)
        startActivity(OdooActivity.newIntent(this, server))
    }
}
