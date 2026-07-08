package com.uitopic.restockmobile.core.notifications.domain.repositories

interface PushTokenRepository {
    suspend fun registerPushToken(userId: Int, token: String): Result<Unit>
}
