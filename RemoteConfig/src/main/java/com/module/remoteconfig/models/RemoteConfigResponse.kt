package com.module.remoteconfig.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
data class RemoteConfigResponse(

    @SerializedName("app_open_splash")
    var appOpenSplash: RemoteModel? = RemoteModel(),

    @SerializedName("app_open_in_app")
    var appOpenInApp: RemoteModel? = RemoteModel(),

    @SerializedName("banner_splash")
    var bannerSplash: RemoteModel? = RemoteModel(),

    @SerializedName("interstitial_function")
    var interstitialFunction: RemoteModel? = RemoteModel(),

    @SerializedName("native_on_boarding")
    var nativeOnBoarding: RemoteModel? = RemoteModel(),

    @SerializedName("native_on_boarding_2")
    var nativeOnBoarding2: RemoteModel? = RemoteModel(),

    @SerializedName("native_exit")
    var nativeExit: RemoteModel? = RemoteModel(),

    @SerializedName("native_functions")
    var nativeFunctions: RemoteModel? = RemoteModel(),

    @SerializedName("native_home")
    var nativeHome: RemoteModel? = RemoteModel(),

    @SerializedName("native_language")
    var nativeLanguage: RemoteModel? = RemoteModel(),

    @SerializedName("show_button")
    var showButton: RemoteModel? = RemoteModel(),

    @SerializedName("test_string")
    var testString: RemoteModel? = RemoteModel(),


    @SerializedName("test_string_2")
    var testString2: RemoteModel? = RemoteModel(),

    @SerializedName("version_number")
    var version: RemoteModel? = RemoteModel(),


)

