package com.module.remoteconfig.states

sealed class RemoteConfigSource {
    object Remote : RemoteConfigSource()
    object Cache : RemoteConfigSource()
    object Default : RemoteConfigSource()
}