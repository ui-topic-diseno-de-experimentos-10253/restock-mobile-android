package com.uitopic.restockmobile.core.notifications.data.remote.services

import com.uitopic.restockmobile.core.notifications.data.remote.models.PushTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT

interface PushTokenApiService {
    @PUT("mobile/push-token")
    suspend fun registerPushToken(
        @Body request: PushTokenRequest
    ): Response<Unit>
}
