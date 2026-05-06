package com.wuodoo.mobile.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.wuodoo.mobile.R
import com.wuodoo.mobile.data.ServerInfo
import com.wuodoo.mobile.data.ServerRepository
import com.wuodoo.mobile.databinding.ActivityConnectBinding
import com.wuodoo.mobile.network.OdooApi
import com.wuodoo.mobile.ui.adapter.SavedServerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConnectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectBinding
    private var protocol = "http"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupProtocolToggle()
        setupConnectButton()
        setupSavedServers()
    }

    override fun onResume() {
        super.onResume()
        setupSavedServers()
    }

    private fun setupProtocolToggle() {
        updateProtocolButtons()
        binding.btnHttp.setOnClickListener {
            protocol = "http"
            updateProtocolButtons()
        }
        binding.btnHttps.setOnClickListener {
            protocol = "https"
            updateProtocolButtons()
        }
    }

    private fun updateProtocolButtons() {
        if (protocol == "http") {
            binding.btnHttp.backgroundTintList  = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
            binding.btnHttp.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.btnHttps.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.border))
            binding.btnHttps.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        } else {
            binding.btnHttps.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
            binding.btnHttps.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.btnHttp.backgroundTintList  = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.border))
            binding.btnHttp.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        }
    }

    private fun setupConnectButton() {
        binding.btnConnect.setOnClickListener { doConnect() }
        binding.inputHost.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                doConnect(); true
            } else false
        }
    }

    private fun doConnect() {
        val host = binding.inputHost.text.toString().trim()
        if (host.isEmpty()) {
            showError("Masukkan alamat server terlebih dahulu.")
            return
        }
        hideError()
        setLoading(true)

        val baseUrl = buildBaseUrl(protocol, host)

        CoroutineScope(Dispatchers.IO).launch {
            val result = OdooApi.fetchDatabases(baseUrl)
            withContext(Dispatchers.Main) {
                setLoading(false)
                result.fold(
                    onSuccess = { dbs ->
                        val intent = DatabaseActivity.newIntent(this@ConnectActivity, host, protocol, dbs)
                        startActivity(intent)
                    },
                    onFailure = {
                        // Fallback ke manual input database
                        val intent = DatabaseActivity.newIntent(this@ConnectActivity, host, protocol, emptyList())
                        startActivity(intent)
                    }
                )
            }
        }
    }

    private fun setupSavedServers() {
        val servers = ServerRepository.getSavedServers()
        if (servers.isEmpty()) {
            binding.savedServersSection.visibility = View.GONE
            return
        }
        binding.savedServersSection.visibility = View.VISIBLE
        binding.rvSavedServers.layoutManager = LinearLayoutManager(this)
        binding.rvSavedServers.adapter = SavedServerAdapter(
            servers = servers,
            onItemClick = { server ->
                if (server.database.isNotEmpty()) {
                    ServerRepository.saveLastServer(server)
                    startActivity(OdooActivity.newIntent(this, server))
                } else {
                    binding.inputHost.setText(server.host)
                    protocol = server.protocol
                    binding.btnHttp.isSelected  = protocol == "http"
                    binding.btnHttps.isSelected = protocol == "https"
                    doConnect()
                }
            },
            onDeleteClick = { index ->
                ServerRepository.deleteServer(index)
                setupSavedServers()
            }
        )
    }

    private fun setLoading(loading: Boolean) {
        binding.btnConnect.isEnabled = !loading
        binding.btnConnect.text = if (loading) "" else "Sambungkan"
        binding.btnConnectLoader.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(msg: String) {
        binding.connectError.text = msg
        binding.connectError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.connectError.visibility = View.GONE
    }

    private fun buildBaseUrl(proto: String, host: String): String {
        val h = host.trimEnd('/')
        return if (h.startsWith("http://") || h.startsWith("https://")) h
        else "$proto://$h"
    }
}
