package com.mobile.test.application.app

import android.app.Application
import com.ads.adsmodule.ads.open_app.AppOpenAdManager
import com.google.firebase.FirebaseApp
import com.mobile.test.application.core.UnifiedConsentManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    var appOpenAdManager: AppOpenAdManager? = null

    override fun onCreate() {
        super.onCreate()
        appOpenAdManager= AppOpenAdManager(this)
        FirebaseApp.initializeApp(this)

        UnifiedConsentManager.initialize(this)

    }

}




