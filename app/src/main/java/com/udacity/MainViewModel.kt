package com.udacity

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainViewModel(private val appContext: Application) : ViewModel() {

    private var _selectedOption: Download? = null

    private val _message: MutableLiveData<Message?> = MutableLiveData(null)
    val message: LiveData<Message?>
        get() = _message

    private val _downloadUrl: MutableLiveData<DownloadInfo?> = MutableLiveData(null)
    val downloadUrl: LiveData<DownloadInfo?>
        get() = _downloadUrl

    private val _downloadToCancel: MutableLiveData<Long?> = MutableLiveData(null)
    val downloadToCancel: LiveData<Long?>
        get() = _downloadToCancel

    private var scheduledDownload: Pair<Long, Download>? = null

    fun addDownload(download: Download, id: Long) {
        _downloadToCancel.value = scheduledDownload?.first
        scheduledDownload = id to download
        _downloadUrl.value = null
    }

    fun onClick() {
        if (_selectedOption == null) {
            _message.value = Message.Toast(appContext.getString(R.string.select_radio_button))
        } else {
            _downloadUrl.value = DownloadInfo(
                _selectedOption!!,
                appContext.getString(R.string.app_name),
                appContext.getString(R.string.app_description)
            )
        }
    }

    fun select(download: Download) {
        _selectedOption = download
    }

    fun setMessageShown() {
        _message.value = null
    }

    fun onDownloadComplete(id: Long?, downloadStatus: Int) {
        scheduledDownload?.let {
            if (it.first != id) return
            _message.value = Message.Notification(
                it.first.toInt(),
                appContext.getString(R.string.notification_title),
                appContext.getString(R.string.notification_description),
                appContext.getString(R.string.channel_id),
                appContext.getString(R.string.channel_name),
                appContext.getString(R.string.action_text),
                it.second,
                downloadStatus
            )
        }
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct view model")
        }
    }

    sealed class Message {
        data class Toast(val text: String) : Message()
        data class Notification(
            val id: Int,
            val title: String,
            val description: String,
            val channelId: String,
            val channelName: String,
            val actionText: String,
            val download: Download,
            val downloadStatus: Int
        ) : Message()
    }

    data class DownloadInfo(
        val download: Download,
        val notificationTitle: String,
        val notificationDescription: String
    )
}