package com.wuodoo.mobile.ui

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wuodoo.mobile.data.ServerInfo
import com.wuodoo.mobile.databinding.ActivityOdooBinding

class OdooActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOdooBinding
    private lateinit var server: ServerInfo

    companion object {
        private const val EXTRA_SERVER_HOST     = "server_host"
        private const val EXTRA_SERVER_PROTOCOL = "server_protocol"
        private const val EXTRA_SERVER_DATABASE = "server_database"

        fun newIntent(context: Context, server: ServerInfo): Intent {
            return Intent(context, OdooActivity::class.java).apply {
                putExtra(EXTRA_SERVER_HOST,     server.host)
                putExtra(EXTRA_SERVER_PROTOCOL, server.protocol)
                putExtra(EXTRA_SERVER_DATABASE, server.database)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOdooBinding.inflate(layoutInflater)
        setContentView(binding.root)

        server = ServerInfo(
            host     = intent.getStringExtra(EXTRA_SERVER_HOST)     ?: "",
            protocol = intent.getStringExtra(EXTRA_SERVER_PROTOCOL) ?: "http",
            database = intent.getStringExtra(EXTRA_SERVER_DATABASE) ?: ""
        )

        setupWebView()
        setupDownloadListener()

        binding.webView.loadUrl(server.odooUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = binding.webView.settings
        settings.javaScriptEnabled          = true
        settings.domStorageEnabled          = true
        settings.databaseEnabled            = true
        settings.allowFileAccess            = true
        settings.allowContentAccess         = true
        settings.loadWithOverviewMode       = true
        settings.useWideViewPort            = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls        = true
        settings.displayZoomControls        = false
        settings.mixedContentMode           = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.cacheMode                  = WebSettings.LOAD_DEFAULT
        settings.mediaPlaybackRequiresUserGesture = false
        settings.userAgentString            = settings.userAgentString + " WUOdooMobile/1.0"

        // Cookie manager — aktifkan third-party cookies
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(binding.webView, true)

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                binding.progressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView, request: WebResourceRequest, error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    binding.progressBar.visibility = View.GONE
                }
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // Biarkan semua URL dibuka di WebView yang sama
                return false
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                binding.progressBar.progress = newProgress
                binding.progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }

            // Handle file upload (input type=file)
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                // Simpan callback untuk dipakai setelah user pilih file
                fileUploadCallback = filePathCallback
                val intent = fileChooserParams.createIntent()
                try {
                    startActivityForResult(intent, REQUEST_FILE_CHOOSER)
                } catch (e: Exception) {
                    fileUploadCallback = null
                    Toast.makeText(this@OdooActivity, "Tidak bisa membuka file picker", Toast.LENGTH_SHORT).show()
                    return false
                }
                return true
            }
        }
    }

    /**
     * Setup download listener — ini yang membedakan dengan Cordova.
     * Android DownloadManager handle semua download secara native,
     * persis seperti Chrome.
     */
    private fun setupDownloadListener() {
        binding.webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            try {
                // Ambil filename dari Content-Disposition header
                val filename = extractFilename(contentDisposition, url)

                // Ambil cookie dari WebView untuk autentikasi
                val cookies = CookieManager.getInstance().getCookie(url) ?: ""

                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setTitle(filename)
                    setDescription("Mengunduh dari Odoo...")
                    setMimeType(mimetype)
                    addRequestHeader("Cookie", cookies)
                    addRequestHeader("User-Agent", userAgent)
                    // Simpan ke folder Downloads publik
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
                    // Tampilkan di notification bar
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    // Izinkan download via WiFi dan mobile data
                    setAllowedOverMetered(true)
                    setAllowedOverRoaming(true)
                }

                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)

                Toast.makeText(this, "⏳ Mengunduh $filename...", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this, "❌ Gagal memulai download: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Ekstrak nama file dari Content-Disposition header atau URL.
     */
    private fun extractFilename(contentDisposition: String?, url: String): String {
        // Coba dari Content-Disposition: attachment; filename="namafile.pdf"
        if (!contentDisposition.isNullOrEmpty()) {
            val patterns = listOf(
                Regex("""filename\*=UTF-8''(.+)""", RegexOption.IGNORE_CASE),
                Regex("""filename="([^"]+)"""", RegexOption.IGNORE_CASE),
                Regex("""filename=([^\s;]+)""", RegexOption.IGNORE_CASE)
            )
            for (pattern in patterns) {
                val match = pattern.find(contentDisposition)
                if (match != null) {
                    return Uri.decode(match.groupValues[1]).trim()
                }
            }
        }

        // Coba dari URL parameter filename=
        val uri = Uri.parse(url)
        uri.getQueryParameter("filename")?.let { return it }

        // Coba dari path URL
        val path = uri.path ?: ""
        val lastSegment = path.substringAfterLast('/')
        if (lastSegment.contains('.')) return lastSegment

        // Fallback
        return "odoo_download_${System.currentTimeMillis()}.bin"
    }

    // ── File Upload ──────────────────────────────────────────────────────────
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private val REQUEST_FILE_CHOOSER = 1001

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FILE_CHOOSER) {
            val results = if (resultCode == RESULT_OK && data != null) {
                WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            } else null
            fileUploadCallback?.onReceiveValue(results)
            fileUploadCallback = null
        }
    }

    // ── Back button ──────────────────────────────────────────────────────────
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.canGoBack()) {
            binding.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            // Kembali ke ConnectActivity
            val intent = Intent(this, ConnectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}
