package com.mmk.kmpnotifier.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mmk.kmpnotifier.Constants
import com.mmk.kmpnotifier.extensions.shouldShowNotification
import com.mmk.kmpnotifier.notification.Notifier
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.NotifierManagerImpl
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration

internal class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val notifierManager by lazy { NotifierManagerImpl }
    private val notifier: Notifier by lazy { notifierManager.getLocalNotifier() }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("FirebaseMessaging: onNewToken is called")
        notifierManager.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val payloadData = message.data
        val title = message.notification?.title ?: payloadData[Constants.KEY_DATA_TITLE]
        val body = message.notification?.body ?: payloadData[Constants.KEY_DATA_BODY]
        if (title != null || body != null) {
            if (notifierManager.shouldShowNotification())
                notifier.notify(
                    title = title ?: "",
                    body = body ?: "",
                    payloadData = payloadData
                )

            notifierManager.onPushNotification(title = title, body = body)
        }
        if (payloadData.isNotEmpty()) {
            val data =
                payloadData + mapOf(Constants.ACTION_NOTIFICATION_CLICK to Constants.ACTION_NOTIFICATION_CLICK)
            notifierManager.onPushPayloadData(data)
        }
    }
}