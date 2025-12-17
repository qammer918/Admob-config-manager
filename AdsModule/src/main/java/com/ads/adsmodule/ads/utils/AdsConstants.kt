package com.ads.adsmodule.ads.utils

object AdsConstants {
    var isShowingInter = false // handling interstitial and app_open

    var fragmentValidForAppOpenAd = true   // load/show app open into your desire location

    var mCounter = 0 //interstitial ad counter

    var isAppInForeground = true // do not try to show ad when app is backgrounded which causes failed to show error

    var appOpenIsShown = false // do do show interstitial right after app open



}