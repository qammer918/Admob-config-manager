package com.ads.adsmodule.ads.utils

object AdsConstants {
    var isShowingInter = false
    var isLoadingInter = false
    var isShowingAppOpen = false
    var fragmentValidForAppOpenAd =
        true   // will prevent load/show execution if a valid fragment is not reached, if you don't want to show app open ad in a specific screen(e.g premium), in it's creation make this false, in destruction make this true again

    var mCounter = 0

    var isAppInForeground = true

    var appOpenIsShown = false
    var appOpenInAppId = ""


}