package com.uitopic.restockmobile.core.notifications.data.remote.models

import com.google.gson.annotations.SerializedName

data class PushTokenRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("platform") val platform: String = "ANDROID",
    @SerializedName("pushToken") val pushToken: String
)
