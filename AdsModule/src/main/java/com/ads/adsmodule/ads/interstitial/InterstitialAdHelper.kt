package com.ads.adsmodule.ads.interstitial

import android.app.Activity
import com.ads.adsmodule.ads.nativeAd.NativeAdSlot
import com.ads.adsmodule.ads.utils.AdsConstants.appOpenIsShown
import com.ads.adsmodule.ads.utils.AdsConstants.isAppInForeground
import com.ads.adsmodule.ads.utils.AdsConstants.isShowingInter
import com.ads.adsmodule.ads.utils.AdsConstants.mCounter
import com.ads.adsmodule.ads.utils.dismissLoading
import com.ads.adsmodule.ads.utils.isPremium
import com.ads.adsmodule.ads.utils.logD
import com.ads.adsmodule.ads.utils.showLoading
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


object InterstitialAdHelper {
    val interstitialAds = mutableMapOf<InterstitialSlot, InterstitialAd?>()

    private val isLoadingMap = mutableMapOf<InterstitialSlot, Boolean>()

    private var adCallback: ((InterstitialSlot, String) -> Unit)? = null

    private val isAdShownMap = mutableMapOf<InterstitialSlot, Boolean>()


    fun setAdCallback(callback: (InterstitialSlot, String) -> Unit) {
        adCallback = callback
    }


    // ================================
    // 🔹 PRELOAD MODE
    // ================================
    fun preloadAd(context: Activity, slot: InterstitialSlot, adId: String) {
        if (isPremium() || adId.isEmpty()) {
            adCallback?.invoke(slot, "skipped_premium_or_empty_id")
            return
        }


        if (appOpenIsShown) {
            appOpenIsShown = false
            return
        }

        if (isLoadingMap[slot] == true || interstitialAds[slot] != null) {
            adCallback?.invoke(slot, "already_loading_or_cached")
            return
        }

        isLoadingMap[slot] = true
        adCallback?.invoke(slot, "loading")

        InterstitialAd.load(
            context,
            adId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAds[slot] = null
                    isLoadingMap[slot] = false
                    adCallback?.invoke(slot, "failed: ${error.message}")
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAds[slot] = ad
                    isLoadingMap[slot] = false
                    setupCallbacks(slot, ad)
                    adCallback?.invoke(slot, "loaded")
                }
            }
        )
    }

    // ================================
    // 🔹 SHOW (PRELOADED) MODE
    // ================================
    fun showPreloadedAd(activity: Activity, slot: InterstitialSlot, onDismiss: () -> Unit) {
        if (isPremium()) {
            adCallback?.invoke(slot, "skipped_premium")
            onDismiss()
            return
        }

        val ad = interstitialAds[slot]
        if (ad == null) {
            adCallback?.invoke(slot, "no_cached_ad")
            onDismiss()
            return
        }

        adCallback?.invoke(slot, "showing")

        // ✅ Pass onDismiss callback into setupCallbacks
        setupCallbacks(slot, ad, onDismiss)

        ad.show(activity)
    }

    // ================================
    // 🔹 ON-DEMAND MODE
    // ================================
    fun showOnDemandAd(
        activity: Activity,
        slot: InterstitialSlot,
        adId: String,
        counter: Int = 0,
        onDismiss: () -> Unit
    ) {

        setAdCallback { slot, status ->
            logD("interstitialAds", "$slot: $status")
        }

        if (isPremium() || adId.isEmpty()) {
            adCallback?.invoke(slot, "skipped_premium")
            onDismiss()
            return
        }

        if (appOpenIsShown) {
            appOpenIsShown = false
            onDismiss()
            return
        }

        mCounter++
        if (mCounter < counter) {
            adCallback?.invoke(slot, "skipped_counter $mCounter/$counter")
            onDismiss()
            return
        } else {
            mCounter = 0
        }


        isShowingInter = true
        val cached = interstitialAds[slot]
        if (cached != null) {
            setupCallbacks(slot, cached, onDismiss)
            adCallback?.invoke(slot, "using_cached_ad")
            cached.show(activity)
            return
        }

        activity.showLoading("Loading ad...")

        adCallback?.invoke(slot, "loading_on_demand")
        isLoadingMap[slot] = true

        InterstitialAd.load(
            activity,
            adId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isShowingInter = false
                    isLoadingMap[slot] = false
                    interstitialAds[slot] = null
                    activity.dismissLoading()
                    adCallback?.invoke(slot, "failed: ${error.message}")
                    onDismiss()
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAds[slot] = ad
                    isLoadingMap[slot] = false
                    activity.dismissLoading()
                    adCallback?.invoke(slot, "loaded_on_demand")
                    setupCallbacks(slot, ad, onDismiss)
                    if (isAppInForeground) {
                        ad.show(activity)
                    }
                }
            }
        )
    }

    // ================================
    // 🔹 INTERNAL CALLBACKS
    // ================================
    private fun setupCallbacks(
        slot: InterstitialSlot,
        ad: InterstitialAd,
        onDismiss: (() -> Unit)? = null
    ) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdShowedFullScreenContent() {
                adCallback?.invoke(slot, "shown")
                isShowingInter = true
                isAdShownMap[slot] = true
            }

            override fun onAdDismissedFullScreenContent() {
                isShowingInter = false
                adCallback?.invoke(slot, "dismissed")
                interstitialAds[slot] = null
                onDismiss?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                isShowingInter = false
                adCallback?.invoke(slot, "failed_to_show: ${error.message}")
                isAdShownMap[slot] = false
                if (isAppInForeground) {
                    interstitialAds[slot] = null
                }
                onDismiss?.invoke()
            }
        }
    }

    // ================================
    // 🔹 DESTROY HELPERS
    // ================================
    fun destroy(slot: InterstitialSlot) {
            interstitialAds.remove(slot)
            isAdShownMap[slot] = false
            adCallback?.invoke(slot, "destroyed")
    }

    fun destroyAll() {
        interstitialAds.clear()
    }


}


