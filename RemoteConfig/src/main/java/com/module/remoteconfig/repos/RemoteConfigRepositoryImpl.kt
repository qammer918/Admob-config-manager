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
import com.module.remoteconfig.models.AdModel
import com.module.remoteconfig.models.RemoteConfigResponse
import com.module.remoteconfig.prefs.AdConfigPrefs
import com.module.remoteconfig.repos.interfaces.RemoteConfigRepository
import com.module.remoteconfig.states.RemoteConfigSource
import kotlinx.coroutines.tasks.await
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
    override suspend fun getRemoteResponse() {
        // 1️⃣ Apply cached config immediately (if exists)
        applyCachedConfig()
        try {
            setDefaultIds()
            configureFetchInterval()
            remoteConfig.fetchAndActivate().await()

            val key = if (BuildConfig.DEBUG) "test_json" else "prod_json"

            val responseJson = remoteConfig.getString(key)
            if (responseJson.isBlank()) return

            // 2️⃣ Save + Apply fresh config
            adConfigPrefs.saveAdConfig(responseJson)

            val remoteResponse =
                gson.fromJson(responseJson, RemoteConfigResponse::class.java)

            Log.d("TAG-->>", "getRemoteResponse(): $remoteResponse")
            applyAdConfig(remoteResponse)


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override suspend fun getRemoteResponsee(): RemoteConfigSource {
        try {
            configureFetchInterval()

            remoteConfig.fetchAndActivate().await()
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

                Log.d("TAG-->>", "getRemoteResponse($key): $adIds")

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

            Log.d("RemoteConfig", "Loaded ad config from SharedPreferences")
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
        Log.d("TAG-->>", "Default ad IDs applied")

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
        showMyButton = getValidValue(showButton)

        Log.d(
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
        """.trimIndent()
        )
    }

    /**
     * =========================
     * SAFE AD ID HELPER
     * =========================
     */
    private fun getValidAdId(adModel: AdModel?): String {
        return if (adModel?.show == true && adModel.adId.isNotBlank()) {
            adModel.adId
        } else {
            ""
        }
    }


    /**
     * =========================
     * SAFE Value HELPER
     * =========================
     */

    private fun getValidValue(adModel: AdModel?): Boolean {
        return adModel?.show == true
    }
}


/*

class RemoteConfigRepositoryImpl @Inject constructor(
    private val context: Context,
    private val remoteConfig: FirebaseRemoteConfig
) : RemoteConfigRepository {

    val adConfig = AdConfigPrefs(context)

    */
/**
 * for single json
 *//*


    override suspend fun getRemoteResponse() {
        try {
            setDefaultIds()
            configureFetchInterval()

            remoteConfig.fetchAndActivate().await()

            val key = if (BuildConfig.DEBUG) {
                "test_json"
            } else {
                ""
            }

            val responseJson = remoteConfig.getString(key)
            if (responseJson.isEmpty()) return

            val adIds = Gson().fromJson(responseJson, RemoteConfigResponse::class.java)
            adConfig.saveAdConfig(responseJson)
            Log.d("TAG-->>", "getRemoteResponse:$adIds")
            applyAdConfig(adIds)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    */
/**
 * for multiple json
 *//*


       override suspend fun getRemoteMultipleJson(vararg keys: String) {
           try {
               setDefaultIds()
               configureFetchInterval()
               remoteConfig.fetchAndActivate().await()

               for (key in keys) {
                   val responseJson = remoteConfig.getString(key)
                   if (responseJson.isEmpty()) continue

                   val adIds = Gson().fromJson(responseJson, RemoteConfigResponse::class.java)
                   Log.d("TAG-->>", "getRemoteResponse($key): $adIds")
                   adConfig.saveAdConfig(responseJson)

   //                applyAdConfig(adIds)
               }
           } catch (e: Exception) {
               e.printStackTrace()
           }
       }


    override fun setDefaultIds() {
        Log.d("TAG-->>", "defaultidset")

//        appOpenSplashId = context.getString(R.string.app_open_splash_id)
//        appOpenInAppId = context.getString(R.string.app_open_in_app_id)
//        bannerSplashId = context.getString(R.string.banner_splash_id)
//        interstitialFunctionId = context.getString(R.string.interstitial_function_id)
//        nativeOnBoardingId = context.getString(R.string.native_onboarding_id)
//        nativeOnBoardingId2 = context.getString(R.string.native_onboarding_id2)
//        nativeExitId = context.getString(R.string.native_exit_id)
//        nativeFunctionsId = context.getString(R.string.native_functions_id)
//        nativeHomeId = context.getString(R.string.native_home_id)
//        nativeLanguageId = context.getString(R.string.native_language_id)
    }

    */
/**
 * Sets Firebase Remote Config fetch interval.
 *//*

    private fun configureFetchInterval() {
        val fetchInterval = */
/*if (BuildConfig.DEBUG)*//*
 0L */
/*else 3_600L*//*
 // 1 hour
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = fetchInterval
        }
        remoteConfig.setConfigSettingsAsync(configSettings)


    }

    */
/**
 * Parses ad configuration and assigns valid ad IDs.
 *//*

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


        Log.d(
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
    """.trimIndent()
        )

    }

    */
/**
 * Helper to return adId only if it’s valid and should be shown.
 *//*

    private fun getValidAdId(adModel: AdModel?): String {
        return if (adModel?.show == true && adModel.adId.isNotEmpty()) {
            adModel.adId
        } else {
            ""
        }
    }




}*/
