package com.module.remoteconfig.utils

import android.util.Log
import com.module.remoteconfig.BuildConfig

fun logD(tag: String = "DEFAULT_TAG", message: String = "") {
    if (BuildConfig.DEBUG) {
        Log.d(tag, message)
    }
}
