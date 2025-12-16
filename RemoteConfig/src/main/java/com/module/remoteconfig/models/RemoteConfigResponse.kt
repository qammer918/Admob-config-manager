package com.module.remoteconfig.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
data class RemoteConfigResponse(

    @SerializedName("app_open_splash")
    var appOpenSplash: AdModel? = AdModel(),

    @SerializedName("app_open_in_app")
    var appOpenInApp: AdModel? = AdModel(),

    @SerializedName("banner_splash")
    var bannerSplash: AdModel? = AdModel(),

    @SerializedName("interstitial_function")
    var interstitialFunction: AdModel? = AdModel(),

    @SerializedName("native_on_boarding")
    var nativeOnBoarding: AdModel? = AdModel(),

    @SerializedName("native_on_boarding_2")
    var nativeOnBoarding2: AdModel? = AdModel(),

    @SerializedName("native_exit")
    var nativeExit: AdModel? = AdModel(),

    @SerializedName("native_functions")
    var nativeFunctions: AdModel? = AdModel(),

    @SerializedName("native_home")
    var nativeHome: AdModel? = AdModel(),

    @SerializedName("native_language")
    var nativeLanguage: AdModel? = AdModel(),

    @SerializedName("show_button")
    var showButton: AdModel? = AdModel(),


)

