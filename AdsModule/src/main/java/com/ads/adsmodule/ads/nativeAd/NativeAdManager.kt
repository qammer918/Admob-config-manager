package com.ads.adsmodule.ads.nativeAd

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.ads.adsmodule.R
import com.ads.adsmodule.ads.utils.beGone
import com.ads.adsmodule.ads.utils.beInvisible
import com.ads.adsmodule.ads.utils.beVisible
import com.ads.adsmodule.ads.utils.isPremium
import com.ads.adsmodule.ads.utils.logD
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView


object NativeAdManager {

    private val nativeAds = mutableMapOf<NativeAdSlot, NativeAd?>()
    private val isLoadingMap = mutableMapOf<NativeAdSlot, Boolean>()
    private val isAdShownMap = mutableMapOf<NativeAdSlot, Boolean>()

    /** Callback per ad slot */
    var onAdLoadResult: (NativeAdSlot, Boolean) -> Unit = { _, _ -> }
    var onAdStatus: (NativeAdSlot, String) -> Unit = { _, _ -> }

    /*    var onNativeAdReady: ((NativeAdSlot, NativeAd?) -> Unit)? = null*/

    /** Callbacks per slot */
    private val onNativeAdReadyMap = mutableMapOf<NativeAdSlot, (NativeAd?) -> Unit>()

    fun loadNativeAd(
        slot: NativeAdSlot,
        activity: Activity,
        adLayoutId: Int,
        adContainer: FrameLayout?,
        adUnitId: String,
        shimmerView: ShimmerFrameLayout?,
        showMedia: Boolean = false
    ) {

        if (isPremium() || adUnitId.isEmpty()) {
            shimmerView?.stopShimmer()
            shimmerView?.beGone()
            adContainer?.beGone()
            return
        }

        adContainer?.beVisible()
        shimmerView?.beVisible()
        showLoadedAd(slot, activity, adLayoutId, adContainer, showMedia)

        if (isLoadingMap[slot] == true) {
            logD("NativeAdManager", "$slot: already loading.")
            return
        }

        isLoadingMap[slot] = true
        logD("NativeAdManager", "$slot: starting ad load...")

        val existingAd = nativeAds[slot]
        val isShown = isAdShownMap[slot] == true

        if (!isShown && existingAd != null) {
            logD("NativeAdManager", "$slot: reusing cached ad.")
            showAdView(activity, adLayoutId, adContainer, existingAd, showMedia)
            isLoadingMap[slot] = false
            return
        }

        val adLoader = AdLoader.Builder(activity, adUnitId)
            .forNativeAd { nativeAd ->
                if (activity.isFinishing || activity.isDestroyed) {
                    nativeAd.destroy()
                    return@forNativeAd
                }

                nativeAds[slot]?.destroy()
                nativeAds[slot] = nativeAd
                onNativeAdReadyMap[slot]?.invoke(nativeAd)
            }
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build())
                    .build()
            )
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    shimmerView?.beInvisible()
                    shimmerView?.stopShimmer()
                    adContainer?.beVisible()
                    isLoadingMap[slot] = false
                    isAdShownMap[slot] = false
                    onAdLoadResult(slot, true)
                    logD("NativeAdManager", "$slot: ad loaded successfully.")
                }

                override fun onAdImpression() {
                    isAdShownMap[slot] = true
                    logD("NativeAdManager", "$slot: impression recorded.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    val msg = "${error.domain} ${error.code}: ${error.message}"
                    Log.e("NativeAdManager", "$slot: failed to load — $msg")

                    destroyAd(slot)
                    shimmerView?.stopShimmer()
                    shimmerView?.beGone()
                    adContainer?.beGone()
                    isLoadingMap[slot] = false
                    isAdShownMap[slot] = false
                    onAdLoadResult(slot, false)
                    onNativeAdReadyMap[slot]?.invoke(null)
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    /** Show specific slot’s ad */
    fun showLoadedAd(
        slot: NativeAdSlot,
        activity: Activity,
        adLayoutId: Int,
        adContainer: FrameLayout?,
        showMedia: Boolean
    ) {

        val cached = nativeAds[slot]
        if (cached != null) {
            showAdView(
                activity,
                adLayoutId,
                adContainer,
                cached,
                showMedia
            )
            return
        }

        onNativeAdReadyMap[slot] = { ad ->
            if (ad != null && !activity.isFinishing && !activity.isDestroyed) {
                showAdView(activity, adLayoutId, adContainer, ad, showMedia)
            }
        }
    }

    private fun showAdView(
        activity: Activity,
        adLayoutId: Int,
        adContainer: FrameLayout?,
        nativeAd: NativeAd,
        showMedia: Boolean
    ) {
        val adView = activity.layoutInflater.inflate(adLayoutId, null) as NativeAdView
        populateAdView(nativeAd, adView, showMedia)
        adContainer?.removeAllViews()
        adContainer?.addView(adView)
    }

    private fun populateAdView(
        nativeAd: NativeAd,
        adView: NativeAdView,
        showMedia: Boolean
    ) {
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        (adView.headlineView as? TextView)?.text = nativeAd.headline

        if (showMedia) {
            adView.mediaView = adView.findViewById(R.id.ad_media)
            adView.mediaView?.mediaContent = nativeAd.mediaContent
            adView.mediaView?.visibility = View.VISIBLE
        } else {
            adView.mediaView = adView.findViewById(R.id.ad_media)
            adView.mediaView?.visibility = View.GONE
            adView.mediaView = null // prevents binding
        }


        (adView.bodyView as? TextView)?.apply {
            text = nativeAd.body
            visibility = if (nativeAd.body.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
        }

        (adView.callToActionView as? TextView)?.apply {
            text = nativeAd.callToAction
            visibility = if (nativeAd.callToAction.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
        }

        (adView.iconView as? ImageView)?.apply {
            if (nativeAd.icon == null) visibility = View.INVISIBLE
            else {
                setImageDrawable(nativeAd.icon?.drawable)
                visibility = View.VISIBLE
            }
        }

        adView.setNativeAd(nativeAd)
    }

    fun destroyAd(slot: NativeAdSlot) {
        nativeAds[slot]?.destroy()
        nativeAds.remove(slot)
        isLoadingMap.remove(slot)
        isAdShownMap.remove(slot)
    }

    fun destroyAllAds() {
        nativeAds.values.forEach { it?.destroy() }
        nativeAds.clear()
        isLoadingMap.clear()
        isAdShownMap.clear()
    }

    fun getCachedAd(slot: NativeAdSlot): NativeAd? = nativeAds[slot]
}