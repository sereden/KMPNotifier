package com.mmk.kmpnotifier.firebase

import cocoapods.FirebaseMessaging.FIRMessaging
import cocoapods.FirebaseMessaging.FIRMessagingDelegateProtocol
import com.mmk.kmpnotifier.notification.NotifierManagerImpl
import com.mmk.kmpnotifier.notification.PushNotifier
import com.prinum.utils.logger.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import platform.UIKit.UIApplication
import platform.UIKit.registerForRemoteNotifications
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@OptIn(ExperimentalForeignApi::class)
internal class FirebasePushNotifierImpl : PushNotifier {

    private val firebaseMessageDelegate by lazy { FirebaseMessageDelegate() }

    companion object {
        private const val TAG = "FirebasePushNotifierImpl"
    }

    init {
        MainScope().launch {
            Logger.d(TAG, "FirebasePushNotifier is initialized")
            FIRMessaging.messaging().delegate = firebaseMessageDelegate
            UIApplication.sharedApplication.registerForRemoteNotifications()
        }

    }


    override suspend fun getToken(): String? = suspendCoroutine { cont ->
        FIRMessaging.messaging().tokenWithCompletion { token, error ->
            cont.resume(token)
            error?.let { Logger.d(TAG, "Error while getting token: $error") }
        }

    }

    override suspend fun deleteMyToken() = suspendCoroutine { cont ->
        FIRMessaging.messaging().deleteTokenWithCompletion {
            cont.resume(Unit)
        }
    }

    override suspend fun subscribeToTopic(topic: String) {
        FIRMessaging.messaging().subscribeToTopic(topic)
    }

    override suspend fun unSubscribeFromTopic(topic: String) {
        FIRMessaging.messaging().unsubscribeFromTopic(topic)
    }


    private class FirebaseMessageDelegate : FIRMessagingDelegateProtocol, NSObject() {
        private val notifierManager by lazy { NotifierManagerImpl }
        override fun messaging(messaging: FIRMessaging, didReceiveRegistrationToken: String?) {
            didReceiveRegistrationToken?.let { token ->
                Logger.d(TAG, "FirebaseMessaging: onNewToken is called")
                notifierManager.onNewToken(didReceiveRegistrationToken)
            }
        }

    }
}