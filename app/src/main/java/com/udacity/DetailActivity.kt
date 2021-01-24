package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_DOWNLOAD_FILE = "extra_download_file"
        const val EXTRA_DOWNLOAD_STATUS = "extra_download_status"
    }

    private val notificationManager: NotificationManager by lazy {
        ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0).let {
            notificationManager.cancel(it)
        }
        val status = (intent.getIntExtra(EXTRA_DOWNLOAD_STATUS, 0))
        val statusMessage = when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> getString(R.string.download_successful)
            DownloadManager.STATUS_PAUSED,
            DownloadManager.STATUS_PENDING,
            DownloadManager.STATUS_RUNNING,
            DownloadManager.STATUS_FAILED -> getString(R.string.download_failed)
            else -> getString(R.string.download_failed)
        }
        val description =
            getString(Download.values()[intent.getIntExtra(EXTRA_DOWNLOAD_FILE, 0)].text)

        file_name.text = description
        file_status.text = statusMessage
        file_status.setTextColor(if (status == DownloadManager.STATUS_SUCCESSFUL) Color.GREEN else Color.RED)

        details_ok_button.setOnClickListener {
//            startActivity(Intent(this, MainActivity::class.java).apply {
//                flags = Intent.
//            })
            finish()
        }
    }

}
