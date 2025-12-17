package com.ads.adsmodule.ads.open_app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ads.adsmodule.ads.utils.AdsConstants
import com.ads.adsmodule.ads.utils.AdsConstants.appOpenIsShown
import com.ads.adsmodule.ads.utils.isPremium
import com.ads.adsmodule.ads.utils.isWindowAttached
import com.ads.adsmodule.ads.utils.logD
import com.ads.adsmodule.ads.utils.safeDismissAppOpenLoading
import com.ads.adsmodule.ads.utils.safeShowAppOpenLoading
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.module.remoteconfig.utils.Constants.appOpenSplashId

class AppOpenAdManager(
    private val application: Application
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {
    val appOpenAds = mutableMapOf<AppOpenSlot, AppOpenAd?>()
    private val isLoadingMap = mutableMapOf<AppOpenSlot, Boolean>()
    private val isShowingMap = mutableMapOf<AppOpenSlot, Boolean>()
    val loadFailedMap = mutableMapOf<AppOpenSlot, Boolean>()

    private var currentActivity: Activity? = null
    private var dialogHostActivity: Activity? = null

    var onAdStatus: ((AppOpenSlot, String) -> Unit)? = null

    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()


    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.Companion.get().lifecycle.addObserver(this)
    }


    fun fetchAd(slot: AppOpenSlot, adUnitId: String) {
        if (isPremium() || adUnitId.isEmpty()) {
            onAdStatus?.invoke(slot, "onAdDismissed")
            return
        }

        if (appOpenAds[slot] != null || isLoadingMap[slot] == true) {
            onAdStatus?.invoke(slot, "already_loaded_or_loading")
            return
        }

        isLoadingMap[slot] = true
        loadFailedMap[slot] = false
        onAdStatus?.invoke(slot, "loading")

        AppOpenAd.load(
            application,
            adUnitId,
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAds[slot] = ad
                    isLoadingMap[slot] = false
                    loadFailedMap[slot] = false
                    onAdStatus?.invoke(slot, "onAdLoaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAds[slot] = null
                    isLoadingMap[slot] = false
                    loadFailedMap[slot] = true
                    onAdStatus?.invoke(slot, "onAdFailedToLoad")
                }
            }
        )
    }

    /** show ad (if available) */


    fun showAdIfAvailable(slot: AppOpenSlot, onDismissed: () -> Unit) {

        if (isPremium()) {
            onAdStatus?.invoke(slot, "skipped_premium")
            onDismissed()
            return
        }

        val ad = appOpenAds[slot]
        val failed = loadFailedMap[slot] == true
        val showing = isShowingMap[slot] == true

        if (failed || ad == null) {
            onAdStatus?.invoke(slot, "no_ad_available")
            onDismissed()
            return
        }

        if (showing) {
            onAdStatus?.invoke(slot, "already_showing")
            onDismissed()
            return
        }

        val activity = currentActivity

        if (activity == null) {
            onAdStatus?.invoke(slot, "no_current_activity")
            onDismissed()
            return
        }

        // ❌ Never show AppOpen inside AdActivity
        if (activity is AdActivity) {
            onAdStatus?.invoke(slot, "skip_adactivity")
            onDismissed()
            return
        }

        // Show loading only on REAL activity

        if (!activity.isFinishing && !activity.isDestroyed && activity.isWindowAttached()) {
            dialogHostActivity = activity
            try {
                activity.safeShowAppOpenLoading()
            } catch (_: Exception) {
            }
        } else {
            // we don't show dialog if the activity window isn't attached (avoids attach/detach races)
            dialogHostActivity = null
        }

        onAdStatus?.invoke(slot, "showing")

        ad.fullScreenContentCallback = createFullScreenCallback(slot, onDismissed)

        try {
            if (activity !is AdActivity) {
                ad.show(activity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dialogHostActivity?.safeDismissAppOpenLoading()
            dialogHostActivity = null
            onDismissed()
        }
    }


    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (currentActivity != null && !AdsConstants.isShowingInter && AdsConstants.fragmentValidForAppOpenAd) {
            try {
                showAdIfAvailable(AppOpenSlot.MAIN) {
                    if (AdsConstants.fragmentValidForAppOpenAd) {
                        fetchAd(AppOpenSlot.MAIN, appOpenSplashId)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun createFullScreenCallback(
        slot: AppOpenSlot,
        onDismissed: () -> Unit
    ) = object : FullScreenContentCallback() {

        override fun onAdShowedFullScreenContent() {
            isShowingMap[slot] = true
            appOpenIsShown = true
            onAdStatus?.invoke(slot, "shown")
        }

        override fun onAdDismissedFullScreenContent() {
//            currentActivity?.dismissAppOpenLoading()

            dialogHostActivity?.let { host ->
                if (!host.isFinishing && !host.isDestroyed) {
                    host.safeDismissAppOpenLoading()
                } else {
                    // host is dead — ensure dialog ref removed
                }
            }
            dialogHostActivity = null
            appOpenAds[slot] = null
            isShowingMap[slot] = false
            appOpenIsShown = true
            onAdStatus?.invoke(slot, "dismissed")
            onDismissed()
        }

        override fun onAdFailedToShowFullScreenContent(error: AdError) {
            dialogHostActivity?.let { host ->
                if (!host.isFinishing && !host.isDestroyed) {
                    host.safeDismissAppOpenLoading()
                } else {
                    // host is dead — ensure dialog ref removed
                }
            }
            dialogHostActivity = null
            appOpenAds[slot] = null
            isShowingMap[slot] = false
            appOpenIsShown = false
            onAdStatus?.invoke(slot, "failed_to_show")
            onDismissed()
        }
    }

    /** Destroy ad reference manually */
    fun destroyAd() {
        appOpenAds.clear()
        currentActivity = null

    }

    fun destroySlot(slot: AppOpenSlot) {
        appOpenAds[slot] = null
        isShowingMap.remove(slot)
        isLoadingMap.remove(slot)
        loadFailedMap.remove(slot)
        onAdStatus?.invoke(slot, "destroyed")
    }

    // region ActivityLifecycleCallbacks
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity


    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {

    }
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

}