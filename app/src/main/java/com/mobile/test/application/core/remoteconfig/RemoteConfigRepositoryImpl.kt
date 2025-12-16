//package com.mobile.test.application.core.remoteconfig
//
//import android.content.Context
//import android.util.Log
//import com.google.firebase.remoteconfig.FirebaseRemoteConfig
//import com.google.firebase.remoteconfig.remoteConfigSettings
//import com.google.gson.Gson
//import com.mobile.test.application.BuildConfig
//import com.mobile.test.application.core.remoteconfig.Constants.appOpenInAppId
//import com.mobile.test.application.core.remoteconfig.Constants.appOpenSplashId
//import com.mobile.test.application.core.remoteconfig.Constants.bannerSplashId
//import com.mobile.test.application.core.remoteconfig.Constants.interstitialFunctionId
//import com.mobile.test.application.core.remoteconfig.Constants.nativeExitId
//import com.mobile.test.application.core.remoteconfig.Constants.nativeFunctionsId
//import com.mobile.test.application.core.remoteconfig.Constants.nativeHomeId
//import com.mobile.test.application.core.remoteconfig.Constants.nativeLanguageId
//import com.mobile.test.application.core.remoteconfig.Constants.nativeOnBoardingId
//import com.mobile.test.application.core.remoteconfig.Constants.nativeOnBoardingId2
//import com.module.remoteconfig.RemoteConfigRepository
//import kotlinx.coroutines.tasks.await
//import javax.inject.Inject
//
//class RemoteConfigRepositoryImpl @Inject constructor(
//    private val context: Context,
//    private val remoteConfig: FirebaseRemoteConfig
//) : RemoteConfigRepository {
//
//
////    override suspend fun getRemoteResponse() {
////        try {
////            setDefaultIds()
////            configureFetchInterval()
////
////            remoteConfig.fetchAndActivate().await()
////
////            val key = if (BuildConfig.DEBUG) {
////                "test_json"
////            } else {
////                ""
////            }
////
////            val responseJson = remoteConfig.getString(key)
////            if (responseJson.isEmpty()) return
////
////            val adIds = Gson().fromJson(responseJson, RemoteConfigResponse::class.java)
////            Log.d("TAG-->>", "getRemoteResponse:$adIds")
////            applyAdConfig(adIds)
////        } catch (e: Exception) {
////            e.printStackTrace()
////        }
////    }
//
//
//    override suspend fun getRemoteResponse(vararg keys: String) {
//        try {
//            setDefaultIds()
//            configureFetchInterval()
//            remoteConfig.fetchAndActivate().await()
//
//            for (key in keys) {
//                val responseJson = remoteConfig.getString(key)
//                if (responseJson.isEmpty()) continue
//
//                val adIds = Gson().fromJson(responseJson, RemoteConfigResponse::class.java)
//                Log.d("TAG-->>", "getRemoteResponse($key): $adIds")
//                applyAdConfig(adIds)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//
//    override fun setDefaultIds() {
//        Log.d("TAG-->>", "defaultidset")
//
////        appOpenSplashId = context.getString(R.string.app_open_splash_id)
////        appOpenInAppId = context.getString(R.string.app_open_in_app_id)
////        bannerSplashId = context.getString(R.string.banner_splash_id)
////        interstitialFunctionId = context.getString(R.string.interstitial_function_id)
////        nativeOnBoardingId = context.getString(R.string.native_onboarding_id)
////        nativeOnBoardingId2 = context.getString(R.string.native_onboarding_id2)
////        nativeExitId = context.getString(R.string.native_exit_id)
////        nativeFunctionsId = context.getString(R.string.native_functions_id)
////        nativeHomeId = context.getString(R.string.native_home_id)
////        nativeLanguageId = context.getString(R.string.native_language_id)
//    }
//
//    /**
//     * Sets Firebase Remote Config fetch interval.
//     */
//    private fun configureFetchInterval() {
//        val fetchInterval = if (BuildConfig.DEBUG) 0L else 3_600L // 1 hour
//        val configSettings = remoteConfigSettings {
//            minimumFetchIntervalInSeconds = fetchInterval
//        }
//        remoteConfig.setConfigSettingsAsync(configSettings)
//
//
//    }
//
//    /**
//     * Parses ad configuration and assigns valid ad IDs.
//     */
//    private fun applyAdConfig(adIds: RemoteConfigResponse) = with(adIds) {
//        appOpenSplashId = getValidAdId(appOpenSplash)
//        appOpenInAppId = getValidAdId(appOpenInApp)
//        bannerSplashId = getValidAdId(bannerSplash)
//        interstitialFunctionId = getValidAdId(interstitialFunction)
//        nativeOnBoardingId = getValidAdId(nativeOnBoarding)
//        nativeOnBoardingId2 = getValidAdId(nativeOnBoarding2)
//        nativeExitId = getValidAdId(nativeExit)
//        nativeFunctionsId = getValidAdId(nativeFunctions)
//        nativeHomeId = getValidAdId(nativeHome)
//        nativeLanguageId = getValidAdId(nativeLanguage)
//
//
//        Log.d(
//            "RemoteConfig", """
//        🎯 Ad Config Applied:
//        appOpenSplashId = $appOpenSplashId
//        appOpenInAppId = $appOpenInAppId
//        bannerSplashId = $bannerSplashId
//        interstitialFunctionId = $interstitialFunctionId
//        nativeOnBoardingId = $nativeOnBoardingId
//        nativeOnBoardingId2 = $nativeOnBoardingId2
//        nativeExitId = $nativeExitId
//        nativeFunctionsId = $nativeFunctionsId
//        nativeHomeId = $nativeHomeId
//        nativeLanguageId = $nativeLanguageId
//    """.trimIndent()
//        )
//
//    }
//
//    /**
//     * Helper to return adId only if it’s valid and should be shown.
//     */
//    private fun getValidAdId(adModel: AdModel?): String {
//        return if (adModel?.show == true && adModel.adId.isNotEmpty()) {
//            adModel.adId
//        } else {
//            ""
//        }
//    }
//}