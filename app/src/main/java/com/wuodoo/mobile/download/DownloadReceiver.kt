package com.wuodoo.mobile.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Menerima broadcast saat download selesai dari Android DownloadManager.
 */
class DownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (id == -1L) return

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(id)
        val cursor = dm.query(query)

        if (cursor.moveToFirst()) {
            val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val titleCol  = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
            val status = cursor.getInt(statusCol)
            val title  = cursor.getString(titleCol) ?: "File"

            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                Toast.makeText(context, "✅ Tersimpan: $title", Toast.LENGTH_LONG).show()
            } else if (status == DownloadManager.STATUS_FAILED) {
                val reasonCol = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                val reason = cursor.getInt(reasonCol)
                Toast.makeText(context, "❌ Gagal download: $title (code $reason)", Toast.LENGTH_LONG).show()
            }
        }
        cursor.close()
    }
}
