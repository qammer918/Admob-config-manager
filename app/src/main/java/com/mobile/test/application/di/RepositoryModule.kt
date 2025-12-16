package com.mobile.test.application.di

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.module.remoteconfig.repos.interfaces.RemoteConfigRepository
import com.module.remoteconfig.repos.RemoteConfigRepositoryImpl
import com.mobile.test.application.data.repository.UserRepositoryImpl
import com.mobile.test.application.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository = impl

    @Singleton
    @Provides
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return Firebase.remoteConfig
    }

    @Singleton
    @Provides
    fun provideRemoteConfigRepository(
        @ApplicationContext context: Context,
        remoteConfig: FirebaseRemoteConfig
    ): RemoteConfigRepository {
        return RemoteConfigRepositoryImpl(context, remoteConfig)
    }
}
