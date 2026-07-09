package com.uitopic.restockmobile.core.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.uitopic.restockmobile.core.auth.local.TokenManager
import com.uitopic.restockmobile.core.notifications.domain.repositories.PushTokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushNotificationManager @Inject constructor(
    private val tokenManager: TokenManager,
    private val pushTokenRepository: PushTokenRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun syncToken() {
        val userId = tokenManager.getUserId()
        if (userId == -1 || !tokenManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping FCM token sync")
            return
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            if (!token.isNullOrBlank()) {
                sendTokenToBackend(userId, token)
            }
        }
    }

    fun registerToken(token: String) {
        val userId = tokenManager.getUserId()
        if (userId == -1 || !tokenManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping new FCM token registration")
            return
        }
        sendTokenToBackend(userId, token)
    }

    private fun sendTokenToBackend(userId: Int, token: String) {
        scope.launch {
            Log.d(TAG, "Registering FCM token for user $userId: $token")
            pushTokenRepository.registerPushToken(userId, token)
                .onSuccess {
                    Log.d(TAG, "Successfully registered push token with backend")
                }
                .onFailure { exception ->
                    Log.e(TAG, "Failed to register push token with backend", exception)
                }
        }
    }

    companion object {
        private const val TAG = "PushNotificationManager"
    }
}
