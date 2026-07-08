package com.uitopic.restockmobile.core.notifications.data.repositories

import com.uitopic.restockmobile.core.notifications.data.remote.models.PushTokenRequest
import com.uitopic.restockmobile.core.notifications.data.remote.services.PushTokenApiService
import com.uitopic.restockmobile.core.notifications.domain.repositories.PushTokenRepository
import javax.inject.Inject

class PushTokenRepositoryImpl @Inject constructor(
    private val apiService: PushTokenApiService
) : PushTokenRepository {

    override suspend fun registerPushToken(userId: Int, token: String): Result<Unit> {
        return try {
            val response = apiService.registerPushToken(PushTokenRequest(userId = userId, pushToken = token))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to register push token: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
