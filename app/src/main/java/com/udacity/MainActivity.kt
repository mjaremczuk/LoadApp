package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.udacity.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val notificationManager: NotificationManager by lazy {
        ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
    }
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private val downloadManager: DownloadManager by lazy { getSystemService(DOWNLOAD_SERVICE) as DownloadManager }

    val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this,
            MainViewModel.Factory(application)
        ).get(MainViewModel::class.java)
    }

    private var _binding: ActivityMainBinding? = null
    val binding: ActivityMainBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(toolbar)

        binding.content.viewModel = viewModel
        viewModel.message.observe(this) {
            it?.let {
                when (it) {
                    is MainViewModel.Message.Toast -> showToast(it)
                    is MainViewModel.Message.Notification -> showNotification(it)
                }
                viewModel.setMessageShown()
            }
        }
        viewModel.downloadUrl.observe(this) {
            it?.let {
                download(it)
            }
        }
        viewModel.downloadToCancel.observe(this) {
            it?.let {
                cancelDownload(it)
            }
        }
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        _binding = null
    }

    private fun download(download: MainViewModel.DownloadInfo) {
        binding.content.customButton.setOnEndAnimationAction {
            binding.content.customButton.restartAnimation()
        }
        val request =
            DownloadManager.Request(Uri.parse(download.download.url))
                .setTitle(download.notificationTitle)
                .setDescription(download.notificationDescription)
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val id = downloadManager.enqueue(request)
        viewModel.addDownload(download.download, id)
        //schedule observing progress in view model and on update notify button to make 2nd type of progress?
    }

    private fun cancelDownload(id: Long) {
        downloadManager.remove(id)
    }

    private fun showToast(message: MainViewModel.Message.Toast) {
        Toast.makeText(this, message.text, Toast.LENGTH_SHORT).show()
    }

    private fun showNotification(message: MainViewModel.Message.Notification) {
        createChannel(message.channelId, message.channelName)
        createNotificationAction(message)
        val contentPendingIntent = createPendingIntent(message)

        val downloadImage = BitmapFactory.decodeResource(
            applicationContext.resources,
            R.drawable.ic_launcher_background
        )

        val builder = NotificationCompat.Builder(
            this,
            message.channelId
        ).apply {
            setSmallIcon(R.drawable.ic_baseline_cloud_download_24)
            setContentTitle(message.title)
            setContentText(message.description)
            setAutoCancel(true)
            setContentIntent(contentPendingIntent)
            addAction(action)
            setLargeIcon(downloadImage)
        }

        notificationManager.notify(message.id, builder.build())
    }

    private fun createPendingIntent(message: MainViewModel.Message.Notification): PendingIntent {
        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            message.id,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createNotificationAction(message: MainViewModel.Message.Notification) {
        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
            .apply {
                putExtra(DetailActivity.EXTRA_NOTIFICATION_ID, message.id)
                putExtra(DetailActivity.EXTRA_DOWNLOAD_FILE, message.download.ordinal)
                putExtra(DetailActivity.EXTRA_DOWNLOAD_STATUS, message.downloadStatus)
            }

        pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(contentIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        } as PendingIntent

        action =
            NotificationCompat.Action(
                R.drawable.ic_baseline_cloud_download_24,
                message.actionText,
                pendingIntent
            )
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lightColor = Color.GREEN
                enableVibration(true)
                description = channelName
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun getDownloadStatus(id: Long): Int {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = downloadManager.query(query)
        var status = -1
        if (cursor.moveToFirst()) {
            status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        }
        cursor.close()
        return status
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val status = getDownloadStatus(id!!)
            viewModel.onDownloadComplete(id, status)
            binding.content.customButton.setOnEndAnimationAction(null)
        }
    }
}
