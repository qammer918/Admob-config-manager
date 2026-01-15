package com.ads.adsmodule.ads.bannerad

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isNotEmpty
import com.ads.adsmodule.ads.ads_states.AdsStates
import com.ads.adsmodule.ads.utils.isPremium
import com.ads.adsmodule.ads.utils.logD
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError


object BannerAdManager {

     val bannerAds = mutableMapOf<BannerSlot, AdView>()
    private val isLoadingMap = mutableMapOf<BannerSlot, Boolean>()
    private val adCallbacks = mutableMapOf<BannerSlot, (BannerSlot, AdsStates) -> Unit>()

    private val isAdShownMap = mutableMapOf<BannerSlot, Boolean>()
    private fun getAdSize(activity: Activity, container: FrameLayout): AdSize {
        return try {
            val displayMetrics = DisplayMetrics()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = activity.windowManager.currentWindowMetrics
                val bounds = windowMetrics.bounds
                displayMetrics.widthPixels = bounds.width()
                displayMetrics.density = activity.resources.displayMetrics.density
            } else {
                @Suppress("DEPRECATION")
                activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            }

            var adWidthPixels = container.width.toFloat()
            if (adWidthPixels == 0f) adWidthPixels = displayMetrics.widthPixels.toFloat()
            val adWidth = (adWidthPixels / displayMetrics.density).toInt()
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
        } catch (e: Exception) {
            AdSize.BANNER
        }
    }

    fun loadBanner(
        activity: Activity,
        container: FrameLayout,
        slot: BannerSlot,
        adUnitId: String,
        shimmerView: View? = null,
        adCallback: (BannerSlot, AdsStates) -> Unit
    ) {
        if (isPremium() || adUnitId.isEmpty()) {
            shimmerView?.visibility = View.GONE
            container.removeAllViews()
            return
        }

        shimmerView?.visibility = View.VISIBLE
        container.visibility = View.VISIBLE

        adCallbacks[slot] = adCallback


        if (isLoadingMap[slot] == true) {
            shimmerView?.visibility = View.VISIBLE
            adCallback(slot, AdsStates.LOADING)
            return
        }



        val existingAd = bannerAds[slot]
        val isShown = isAdShownMap[slot] == true

        logD("BannerAdManager","isSown:$isShown+$slot+ExistingAd:$existingAd+$slot")

        if (!isShown && existingAd != null) {
            if (existingAd.parent != null) {
                (existingAd.parent as? FrameLayout)?.removeAllViews()
            }
            shimmerView?.visibility = View.GONE
            container.removeAllViews()
            container.addView(existingAd)
            isLoadingMap[slot] = false
            return
        }


/*
        bannerAds[slot]?.let { cachedAd ->
            if (cachedAd.parent != null) {
                (cachedAd.parent as? FrameLayout)?.removeAllViews()
            }
            shimmerView?.visibility = View.GONE
            container.removeAllViews()
            container.addView(cachedAd)
            return
        }*/

        shimmerView?.visibility = View.VISIBLE
        adCallback(slot, AdsStates.LOADING)
        isLoadingMap[slot] = true



        // Destroy previous ad if any
        bannerAds[slot]?.let { oldAd ->
            try {
                oldAd.destroy()
            } catch (_: Exception) {}
            container.removeView(oldAd)
        }
        bannerAds.remove(slot)

        // Create new AdView
        val adView = AdView(activity).apply {
            this.adUnitId = adUnitId
            setAdSize(getAdSize(activity, container))
        }
        bannerAds[slot] = adView
        container.addView(adView)
        adView.loadAd(AdRequest.Builder().build())


        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                shimmerView?.visibility = View.GONE
                isLoadingMap[slot] = false
                adCallback(slot, AdsStates.LOADED)
                isAdShownMap[slot] = false
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                shimmerView?.visibility = View.GONE
                isLoadingMap[slot] = false
                isAdShownMap[slot] = false
                bannerAds.remove(slot)
                container.removeAllViews()
                adCallback(slot, AdsStates.FAILED_TO_LOAD)
            }

            override fun onAdImpression() {
                adCallback(slot, AdsStates.AD_IMPRESSION)
                isAdShownMap[slot] = true
            }

            override fun onAdOpened() {
            }

            override fun onAdClosed() {
            }
        }
    }


    // ✅ Pause/resume/destroy per slot
    fun pauseBanner(slot: BannerSlot) {
        bannerAds[slot]?.pause()
    }

    fun resumeBanner(slot: BannerSlot) {
        bannerAds[slot]?.resume()
    }

    fun destroyBanner(slot: BannerSlot) {
      if (isAdShownMap[slot]==true){
          adCallbacks[slot]?.invoke(slot, AdsStates.DESTROY)
          isAdShownMap[slot]=false
          bannerAds[slot]?.destroy()
          bannerAds.remove(slot)
          isLoadingMap.remove(slot)
          adCallbacks.remove(slot)

      }
    }

    // ✅ Destroy all banners (e.g., on app exit)
    fun destroyAll() {
        bannerAds.values.forEach { it.destroy() }
        bannerAds.clear()
        isLoadingMap.clear()
        adCallbacks.clear()
    }
}
