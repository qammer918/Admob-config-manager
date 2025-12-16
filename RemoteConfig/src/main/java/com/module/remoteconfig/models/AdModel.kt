package com.module.remoteconfig.models

import com.google.gson.annotations.SerializedName

class AdModel(
    @SerializedName("show") var show: Boolean = false,
    @SerializedName("ad_id") var adId: String = ""
)