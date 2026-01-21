package com.module.remoteconfig.repos

import android.content.Context
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.module.remoteconfig.BuildConfig
import com.module.remoteconfig.R
import com.module.remoteconfig.utils.Constants.appOpenInAppId
import com.module.remoteconfig.utils.Constants.appOpenSplashId
import com.module.remoteconfig.utils.Constants.bannerSplashId
import com.module.remoteconfig.utils.Constants.interstitialFunctionId
import com.module.remoteconfig.utils.Constants.nativeExitId
import com.module.remoteconfig.utils.Constants.nativeFunctionsId
import com.module.remoteconfig.utils.Constants.nativeHomeId
import com.module.remoteconfig.utils.Constants.nativeLanguageId
import com.module.remoteconfig.utils.Constants.nativeOnBoardingId
import com.module.remoteconfig.utils.Constants.nativeOnBoardingId2
import com.module.remoteconfig.utils.Constants.showMyButton
import com.module.remoteconfig.models.RemoteConfigResponse
import com.module.remoteconfig.models.RemoteModel
import com.module.remoteconfig.prefs.AdConfigPrefs
import com.module.remoteconfig.repos.interfaces.RemoteConfigRepository
import com.module.remoteconfig.states.RemoteConfigSource
import com.module.remoteconfig.utils.Constants.testingString
import com.module.remoteconfig.utils.Constants.testingString2
import com.module.remoteconfig.utils.Constants.versionNumber
import com.module.remoteconfig.utils.logD
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class RemoteConfigRepositoryImpl @Inject constructor(
    private val context: Context,
    private val remoteConfig: FirebaseRemoteConfig
) : RemoteConfigRepository {

    private val adConfigPrefs = AdConfigPrefs(context)
    private val gson by lazy { Gson() }

    /**
     * =========================
     * SINGLE JSON CONFIG
     * =========================
     */
    override suspend fun getRemoteResponse(): RemoteConfigSource {
        try {
            configureFetchInterval()

            withTimeout(10_000L) {
                remoteConfig.fetchAndActivate().await()
            }

            val key = if (BuildConfig.DEBUG) "test_json" else "prod_json"

            val responseJson = remoteConfig.getString(key)
            if (responseJson.isNotBlank()) {
                // ✅ Remote config available
                adConfigPrefs.saveAdConfig(responseJson)

                val remoteResponse = gson.fromJson(responseJson, RemoteConfigResponse::class.java)
                applyAdConfig(remoteResponse)

                return RemoteConfigSource.Remote
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ✅ Fallback to cached config
        val cachedJson = adConfigPrefs.getAdConfig()
        if (!cachedJson.isNullOrBlank()) {
            return try {
                val cachedResponse = gson.fromJson(cachedJson, RemoteConfigResponse::class.java)
                applyAdConfig(cachedResponse)
                RemoteConfigSource.Cache
            } catch (e: Exception) {
                e.printStackTrace()
                setDefaultIds()
                RemoteConfigSource.Default
            }
        }

        // ✅ Fallback to default IDs
        setDefaultIds()
        return RemoteConfigSource.Default
    }


    /**
     * =========================
     * MULTIPLE JSON CONFIG
     * =========================
     */
    override suspend fun getRemoteMultipleJson(vararg keys: String) {
        // 1️⃣ Apply cached config first
        applyCachedConfig()

        try {
            setDefaultIds()
            configureFetchInterval()
            remoteConfig.fetchAndActivate().await()

            for (key in keys) {
                val responseJson = remoteConfig.getString(key)
                if (responseJson.isBlank()) continue

                adConfigPrefs.saveAdConfig(responseJson)

                val adIds =
                    gson.fromJson(responseJson, RemoteConfigResponse::class.java)

                logD("TAG-->>", "getRemoteResponse($key): $adIds")

                // Apply each config safely
                applyAdConfig(adIds)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * =========================
     * LOAD FROM SHARED PREFS
     * =========================
     */
    private fun applyCachedConfig() {
        val cachedJson = adConfigPrefs.getAdConfig() ?: return

        try {
            val cachedAdIds =
                gson.fromJson(cachedJson, RemoteConfigResponse::class.java)

            logD("RemoteConfig", "Loaded ad config from SharedPreferences")
            applyAdConfig(cachedAdIds)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * =========================
     * DEFAULT IDS (FALLBACK)
     * =========================
     */
    override fun setDefaultIds() {
        logD("TAG-->>", "Default ad IDs applied")

        // XML fallback
        appOpenSplashId = context.getString(R.string.app_open_splash_id)
        appOpenInAppId = context.getString(R.string.app_open_in_app_id)
        bannerSplashId = context.getString(R.string.banner_splash_id)
        interstitialFunctionId = context.getString(R.string.interstitial_function_id)
        nativeOnBoardingId = context.getString(R.string.native_onboarding_id)
        nativeOnBoardingId2 = context.getString(R.string.native_onboarding_id2)
        nativeExitId = context.getString(R.string.native_exit_id)
        nativeFunctionsId = context.getString(R.string.native_functions_id)
        nativeHomeId = context.getString(R.string.native_home_id)
        nativeLanguageId = context.getString(R.string.native_language_id)
        showMyButton = false
    }

    /**
     * =========================
     * REMOTE CONFIG SETTINGS
     * =========================
     */
    private fun configureFetchInterval() {
        val fetchInterval = if (BuildConfig.DEBUG) 0L else 3_600L // 1 hour
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = fetchInterval
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

    }

    /**
     * =========================
     * APPLY AD CONFIG
     * =========================
     */
    private fun applyAdConfig(adIds: RemoteConfigResponse) = with(adIds) {
        appOpenSplashId = getValidAdId(appOpenSplash)
        appOpenInAppId = getValidAdId(appOpenInApp)
        bannerSplashId = getValidAdId(bannerSplash)
        interstitialFunctionId = getValidAdId(interstitialFunction)
        nativeOnBoardingId = getValidAdId(nativeOnBoarding)
        nativeOnBoardingId2 = getValidAdId(nativeOnBoarding2)
        nativeExitId = getValidAdId(nativeExit)
        nativeFunctionsId = getValidAdId(nativeFunctions)
        nativeHomeId = getValidAdId(nativeHome)
        nativeLanguageId = getValidAdId(nativeLanguage)
        showMyButton = getValidBooleanValue(showButton)
        testingString = getValidStringValue(testString)
        testingString2 = getValidStringValue(testString2)
        versionNumber = getValidStringValue(version)

        logD(
            "RemoteConfig", """
            🎯 Ad Config Applied:
            appOpenSplashId = $appOpenSplashId
            appOpenInAppId = $appOpenInAppId
            bannerSplashId = $bannerSplashId
            interstitialFunctionId = $interstitialFunctionId
            nativeOnBoardingId = $nativeOnBoardingId
            nativeOnBoardingId2 = $nativeOnBoardingId2
            nativeExitId = $nativeExitId
            nativeFunctionsId = $nativeFunctionsId
            nativeHomeId = $nativeHomeId
            nativeLanguageId = $nativeLanguageId
            showMyButton = $showMyButton
            testingString = $testingString
            testingString2 = $testingString2
            versionNumber = $versionNumber
        """.trimIndent()
        )
    }

    /**
     * =========================
     * SAFE AD ID HELPER
     * =========================
     */
    private fun getValidAdId(adModel: RemoteModel?): String {
        return if (adModel?.show == true && adModel.value.isNotBlank()) {
            adModel.value
        } else {
            ""
        }
    }


    /**
     * =====================
     * SAFE boolean Value HELPER
     * =====================
     */

    private fun getValidBooleanValue(adModel: RemoteModel?): Boolean {
        return adModel?.show == true
    }

    /**
     * ===================
     * SAFE String Value HELPER
     * ===================
     */

    private fun getValidStringValue(adModel: RemoteModel?): String {
        return adModel?.value ?:""
    }



}
