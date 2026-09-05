package app.pwhs.apexfilemanager.features.wifishare.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import app.pwhs.apexfilemanager.features.wifishare.R
import app.pwhs.apexfilemanager.features.wifishare.server.KtorFileServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WifiShareService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var fileServer: KtorFileServer

    override fun onCreate() {
        super.onCreate()
        fileServer = KtorFileServer(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        when (action) {
            ACTION_STOP -> stopServer()
            ACTION_START -> {
                val port = intent?.getIntExtra(EXTRA_PORT, DEFAULT_PORT) ?: DEFAULT_PORT
                val ip = intent?.getStringExtra(EXTRA_IP) ?: ""
                startServer(ip, port)
            }
        }
        return START_NOT_STICKY
    }

    private fun startServer(ip: String, port: Int) {
        serviceScope.launch {
            _serverState.value = ServerStatus.Starting
            val result = fileServer.start(port)
            result.onSuccess {
                val url = "http://$ip:$port"
                _serverUrl.value = url
                _serverState.value = ServerStatus.Running(url)
                val notification = buildNotification(url)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }.onFailure { error ->
                _serverState.value = ServerStatus.Error(error.localizedMessage ?: "Start failed")
                stopSelf()
            }
        }
    }

    private fun stopServer() {
        serviceScope.launch {
            fileServer.stop()
            _serverState.value = ServerStatus.Stopped
            _serverUrl.value = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.wifishare_title),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(url: String): android.app.Notification {
        val stopIntent = Intent(this, WifiShareService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val activityIntent = packageManager.getLaunchIntentForPackage(packageName)
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.wifishare_notification_title))
            .setContentText(getString(R.string.wifishare_notification_desc, url))
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.wifishare_notification_stop),
                stopPendingIntent
            )
            .setOngoing(true)
            .build()
    }

    sealed interface ServerStatus {
        data object Stopped : ServerStatus
        data object Starting : ServerStatus
        data class Running(val url: String) : ServerStatus
        data class Error(val message: String) : ServerStatus
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "wifishare_channel"
        const val ACTION_START = "app.pwhs.apexfilemanager.wifishare.START"
        const val ACTION_STOP = "app.pwhs.apexfilemanager.wifishare.STOP"
        const val EXTRA_PORT = "extra_port"
        const val EXTRA_IP = "extra_ip"
        const val DEFAULT_PORT = 8080

        private val _serverState = MutableStateFlow<ServerStatus>(ServerStatus.Stopped)
        val serverState: StateFlow<ServerStatus> = _serverState.asStateFlow()

        private val _serverUrl = MutableStateFlow("")
        val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

        fun start(context: Context, ip: String, port: Int = DEFAULT_PORT) {
            val intent = Intent(context, WifiShareService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_IP, ip)
                putExtra(EXTRA_PORT, port)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, WifiShareService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
