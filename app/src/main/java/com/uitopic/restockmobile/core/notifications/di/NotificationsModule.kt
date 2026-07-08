package com.uitopic.restockmobile.core.notifications.di

import com.uitopic.restockmobile.core.notifications.data.repositories.PushTokenRepositoryImpl
import com.uitopic.restockmobile.core.notifications.domain.repositories.PushTokenRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    @Singleton
    abstract fun bindPushTokenRepository(
        impl: PushTokenRepositoryImpl
    ): PushTokenRepository
}
