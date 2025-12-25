package com.module.remoteconfig.repos.interfaces

import com.module.remoteconfig.states.RemoteConfigSource

interface RemoteConfigRepository {

    suspend fun getRemoteResponse()
    suspend fun getRemoteResponsee(): RemoteConfigSource
    suspend fun getRemoteMultipleJson(vararg keys: String) // for multiple json

    fun setDefaultIds()



}