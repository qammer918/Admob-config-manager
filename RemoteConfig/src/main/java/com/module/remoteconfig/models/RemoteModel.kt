package com.module.remoteconfig.models

import com.google.gson.annotations.SerializedName

data class RemoteModel(
    @SerializedName("show") var show: Boolean = false,
    @SerializedName("value") var value: String = ""
)