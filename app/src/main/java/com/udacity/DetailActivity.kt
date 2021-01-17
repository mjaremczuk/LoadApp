package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*

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

        intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)?.let {
            notificationManager.cancel(it)
        }
        when (intent.getIntExtra(EXTRA_DOWNLOAD_STATUS, 0)) {
            DownloadManager.STATUS_PAUSED -> { }
            DownloadManager.STATUS_PENDING -> { }
            DownloadManager.STATUS_RUNNING -> { }
            DownloadManager.STATUS_SUCCESSFUL -> { }
            DownloadManager.STATUS_FAILED -> { }
        }
        val description = getString(intent.getIntExtra(EXTRA_DOWNLOAD_FILE, R.string.custom_download_source))
    }

}
