package com.module.remoteconfig.prefs

import android.content.Context
import androidx.core.content.edit

class AdConfigPrefs(context: Context) {

    private val prefs =
        context.getSharedPreferences("ad_config_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_AD_CONFIG = "remote_ad_config_json"
    }

    fun saveAdConfig(json: String) {
        prefs.edit { putString(KEY_AD_CONFIG, json) }
    }

    fun getAdConfig(): String? {
        return prefs.getString(KEY_AD_CONFIG, null)
    }

    fun clear() {
        prefs.edit { clear() }
    }
}