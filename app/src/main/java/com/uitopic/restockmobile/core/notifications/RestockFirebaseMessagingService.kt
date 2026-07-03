package com.uitopic.restockmobile.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.uitopic.restockmobile.MainActivity
import com.uitopic.restockmobile.R
import com.uitopic.restockmobile.analytics.RestockAnalytics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RestockFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var restockAnalytics: RestockAnalytics

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notificationType = data["notification_type"]
            ?: data["alert_type"]
            ?: "stock_low"
        val supplyId = data["supply_id"] ?: ""
        val title = remoteMessage.notification?.title
            ?: data["title"]
            ?: "Alerta de Inventario"
        val body = remoteMessage.notification?.body
            ?: data["body"]
            ?: "Tienes una actualización relevante en tu inventario."

        // T-37-05: Registrar evento notification_received en Firebase Analytics
        restockAnalytics.trackNotificationReceived(
            notificationType = notificationType,
            supplyId = supplyId
        )

        // T-37-01 & T-37-02: Mostrar notificación local con Deep Link Intent
        showNotification(title, body, notificationType, supplyId)
    }

    private fun showNotification(
        title: String,
        body: String,
        notificationType: String,
        supplyId: String
    ) {
        val channelId = CHANNEL_ID
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Alertas de Inventario"
            val channelDescription = "Notificaciones automáticas sobre stock bajo y vencimiento de insumos"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }

        // T-37-04: Intent para Deep Linking directo al detalle del insumo
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_SUPPLY_ID, supplyId)
            putExtra(EXTRA_NOTIFICATION_TYPE, notificationType)
            putExtra(EXTRA_FROM_NOTIFICATION, true)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            pendingIntentFlags
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "RestockFCMService"
        const val CHANNEL_ID = "inventory_alerts_channel"
        const val EXTRA_SUPPLY_ID = "supply_id"
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_FROM_NOTIFICATION = "from_notification"
    }
}
