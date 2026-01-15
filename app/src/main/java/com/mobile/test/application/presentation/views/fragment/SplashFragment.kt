package com.mobile.test.application.presentation.views.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ads.adsmodule.ads.ads_states.AdsStates
import com.ads.adsmodule.ads.bannerad.BannerAdManager
import com.ads.adsmodule.ads.bannerad.BannerSlot
import com.ads.adsmodule.ads.open_app.AppOpenSlot
import com.ads.adsmodule.ads.utils.isPremium
import com.ads.adsmodule.ads.utils.logD
import com.mobile.test.application.R
import com.mobile.test.application.app.MyApplication
import com.mobile.test.application.core.Constants.adShown
import com.mobile.test.application.core.UnifiedConsentManager
import com.mobile.test.application.core.beGone
import com.mobile.test.application.core.beVisible
import com.mobile.test.application.core.click
import com.mobile.test.application.core.safeNavigate
import com.mobile.test.application.databinding.FragmentSplashBinding
import com.module.remoteconfig.utils.Constants.appOpenSplashId
import com.module.remoteconfig.utils.Constants.bannerSplashId
import com.module.remoteconfig.viewmodel.RemoteConfigViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {
    private var splashTime = 15000L
    private var splashTimeInterval = 1000L
    private var splashAdTimer: CountDownTimer? = null
    private val application: MyApplication?
     get() = activity?.application as? MyApplication
    private val remoteConfigViewModel: RemoteConfigViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        safeBinding?.apply {
            loadAdOrContinue()

            getStarted.click {
                safeNavigate(R.id.UserListFragment)
            }
        }
    }

    private fun loadAdOrContinue() {
        if (!isPremium()) {
            handleAdmobGDPR()
        } else {
            startCountDownTimer(isPremium())
        }
    }


    private fun handleAdmobGDPR() {
        binding.textView.text = "Getting consent"
        activity?.let {
            UnifiedConsentManager.gatherConsentAndInitialize(it) {
                remoteConfigViewModel.fetchRemoteConfig {
                    binding.textView.text = "Loading ads.."
                    loadAds()
                    startCountDownTimer(isPremium())
                }
            }
        }
    }

    fun loadAds() {
        application?.appOpenAdManager?.fetchAd(AppOpenSlot.SPLASH, appOpenSplashId)
        loadAndShowBanner()
    }

    private fun loadAndShowBanner() {
        BannerAdManager.loadBanner(
            activity = requireActivity(),
            container = binding.adContainer,
            slot = BannerSlot.HOME,
            adUnitId = bannerSplashId,
            binding.shimmerLayout.root
        ) { slot, status ->
            logD("BannerAd", "[$slot] Status: $status")
            when (status) {
                AdsStates.FAILED_TO_LOAD -> {
                    showGetStartedButton()
                }
                AdsStates.AD_IMPRESSION -> {
                    showGetStartedButton()
                }
                else -> {
                }
            }
        }
    }

    private fun showGetStartedButton() {
        binding.getStarted.beVisible()
        binding.textView.beGone()
        binding.progressBar.beGone()
    }

    private fun startCountDownTimer(premium: Boolean) {
        splashAdTimer = null
        if (premium) {
            premiumUserTimer()
        } else {
            freeUserTimer()
        }
    }


    private fun freeUserTimer() {
        splashAdTimer = object : CountDownTimer(splashTime, splashTimeInterval) {
            override fun onTick(millisUntilFinished: Long) {
                logD("TAG->", "onTick:$millisUntilFinished ")
                showLoadedAd(millisUntilFinished)
            }

            override fun onFinish() {
                application?.appOpenAdManager?.showAdIfAvailable(AppOpenSlot.SPLASH) {
                    safeNavigate(R.id.UserListFragment)
                }
            }
        }
        splashAdTimer?.start()
    }

    private fun showLoadedAd(millisUntilFinished: Long) {
        val allLoaded = application?.appOpenAdManager?.appOpenAds[AppOpenSlot.SPLASH] != null
        val allFailed = application?.appOpenAdManager?.loadFailedMap[AppOpenSlot.SPLASH]
        logD("TAG->", "onTickOut:$millisUntilFinished ")
        if (allLoaded || allFailed == true) {
            cancelCountDownTimer()
            logD("TAG->", "onTickIn:$millisUntilFinished ")
            application?.appOpenAdManager?.showAdIfAvailable(AppOpenSlot.SPLASH) {
                safeNavigate(R.id.UserListFragment)
            }
        }
    }

    private fun premiumUserTimer() {
        splashAdTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                safeNavigate(R.id.UserListFragment)
            }

        }
        splashAdTimer?.start()
    }

    private fun handleTimerOnResume() {
        if (isPremium()) {
            startCountDownTimer(true)
        } else {
            if (splashAdTimer != null && !adShown) {
                startCountDownTimer(isPremium())
            }
        }
    }


    private fun handleTimerOnPause() {
        cancelCountDownTimer()
    }

    override fun onPause() {
        super.onPause()
        handleTimerOnPause()
    }

    override fun onResume() {
        super.onResume()
        handleTimerOnResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelCountDownTimer()
        adShown = false

    }

    override fun onDestroyView() {
        application?.appOpenAdManager?.destroyAd()
//        application?.appOpenAdManager?.onAdStatus = null
        BannerAdManager.destroyBanner(BannerSlot.HOME)
        super.onDestroyView()

    }

    private fun cancelCountDownTimer() {
        splashAdTimer?.cancel()
    }


}