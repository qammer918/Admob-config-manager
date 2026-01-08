package com.mobile.test.application.core

import android.app.Activity
import android.content.Context
import android.util.Log
import com.ads.adsmodule.ads.utils.logD
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.*
import java.util.EnumMap

object UnifiedConsentManager {
    private lateinit var consentInformation: ConsentInformation

    fun interface OnConsentGatheringCompleteListener {
        fun consentGatheringComplete(error: FormError?)
    }

    fun initialize(context: Context) {
        consentInformation = UserMessagingPlatform.getConsentInformation(context)
    }

    val canRequestAds: Boolean
        get() = ::consentInformation.isInitialized && consentInformation.canRequestAds()
    val isPrivacyOptionsRequired: Boolean
        get() = ::consentInformation.isInitialized && consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Gathers user consent and automatically initializes AdMob if allowed.
     */
    fun gatherConsentAndInitialize(
        activity: Activity,
        onConsentGatheringCompleteListener: OnConsentGatheringCompleteListener
    ) {
        if (!::consentInformation.isInitialized) {
            logD(
                "ConsentManager",
                "❌ You must call initialize(context) before using gatherConsentAndInitialize()"
            )
            onConsentGatheringCompleteListener.consentGatheringComplete(null)
            return
        }

        try {
            val params = ConsentRequestParameters.Builder().build()

            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    try {
                        if (!activity.isFinishing) {
                            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                                handleConsentResult(activity)
                                // ✅ Automatically initialize ads after consent
                                if (canRequestAds) {
                                    initializeMobileAds(activity) {
                                        onConsentGatheringCompleteListener.consentGatheringComplete(
                                            formError
                                        )
                                    }
                                } else {
                                    onConsentGatheringCompleteListener.consentGatheringComplete(
                                        formError
                                    )
                                }
                            }
                        }
                    } catch (_: Exception) {
                        onConsentGatheringCompleteListener.consentGatheringComplete(null)
                    }
                },
                { requestConsentError ->
                    onConsentGatheringCompleteListener.consentGatheringComplete(requestConsentError)
                }
            )
        } catch (_: Exception) {
            onConsentGatheringCompleteListener.consentGatheringComplete(null)
        }
    }

    /**
     * Handles setting Firebase consent state based on the UMP consent status.
     */
    private fun handleConsentResult(activity: Activity) {
        val consentMap: MutableMap<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus> =
            EnumMap(FirebaseAnalytics.ConsentType::class.java)

        when (consentInformation.consentStatus) {
            ConsentInformation.ConsentStatus.OBTAINED -> {
                consentMap[FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE] =
                    FirebaseAnalytics.ConsentStatus.GRANTED
                consentMap[FirebaseAnalytics.ConsentType.AD_STORAGE] =
                    FirebaseAnalytics.ConsentStatus.GRANTED
                consentMap[FirebaseAnalytics.ConsentType.AD_USER_DATA] =
                    FirebaseAnalytics.ConsentStatus.GRANTED
                consentMap[FirebaseAnalytics.ConsentType.AD_PERSONALIZATION] =
                    FirebaseAnalytics.ConsentStatus.GRANTED

                logD("ConsentManager", "Consent OBTAINED -> GRANTED")
            }

            ConsentInformation.ConsentStatus.REQUIRED -> {
                consentMap[FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE] =
                    FirebaseAnalytics.ConsentStatus.DENIED
                consentMap[FirebaseAnalytics.ConsentType.AD_STORAGE] =
                    FirebaseAnalytics.ConsentStatus.DENIED
                consentMap[FirebaseAnalytics.ConsentType.AD_USER_DATA] =
                    FirebaseAnalytics.ConsentStatus.DENIED
                consentMap[FirebaseAnalytics.ConsentType.AD_PERSONALIZATION] =
                    FirebaseAnalytics.ConsentStatus.DENIED

                logD("ConsentManager", "Consent REQUIRED -> DENIED")
            }

            ConsentInformation.ConsentStatus.NOT_REQUIRED -> {
                consentMap[FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE] =
                    FirebaseAnalytics.ConsentStatus.GRANTED
                consentMap[FirebaseAnalytics.ConsentType.AD_STORAGE] =
                    FirebaseAnalytics.ConsentStatus.GRANTED
                consentMap[FirebaseAnalytics.ConsentType.AD_USER_DATA] =
                    FirebaseAnalytics.ConsentStatus.GRANTED
                consentMap[FirebaseAnalytics.ConsentType.AD_PERSONALIZATION] =
                    FirebaseAnalytics.ConsentStatus.GRANTED

                logD("ConsentManager", "Consent NOT_REQUIRED -> default GRANTED")
            }

            else -> {
                consentMap[FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE] =
                    FirebaseAnalytics.ConsentStatus.DENIED
                consentMap[FirebaseAnalytics.ConsentType.AD_STORAGE] =
                    FirebaseAnalytics.ConsentStatus.DENIED
                consentMap[FirebaseAnalytics.ConsentType.AD_USER_DATA] =
                    FirebaseAnalytics.ConsentStatus.DENIED
                consentMap[FirebaseAnalytics.ConsentType.AD_PERSONALIZATION] =
                    FirebaseAnalytics.ConsentStatus.DENIED

                logD("ConsentManager", "Consent UNKNOWN -> DENIED fallback")
            }
        }

        if (!activity.isFinishing) {
            FirebaseAnalytics.getInstance(activity).setConsent(consentMap)
        }
    }

    /**
     * Initializes the Mobile Ads SDK safely.
     */
    private fun initializeMobileAds(activity: Activity, onSuccess: (() -> Unit)) {
        try {
            CoroutineScope(Dispatchers.Default).launch {
                MobileAds.initialize(activity) {
                    logD("AdsInit", "✅ Mobile Ads initialized successfully")
                    onSuccess.invoke()
                }
            }
        } catch (e: Exception) {
            logD("AdsInit", "❌ Failed to initialize Mobile Ads: ${e.message}")
            onSuccess.invoke()
        }
    }

    fun showPrivacyOptionsForm(
        activity: Activity,
        onConsentFormDismissedListener: ConsentForm.OnConsentFormDismissedListener
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
    }
}


/*
object UnifiedConsentManager {

    private const val TAG = "UnifiedConsentManager"

    private var consentInformation: ConsentInformation? = null
    private var isMobileAdsInitialized = false
    private var isProcessingConsent = false

    // 🧠 Entry point
    fun gatherUserConsent(
        activity: Activity,
        onComplete: (Boolean) -> Unit
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onComplete(false)
            return
        }

        if (isProcessingConsent) {
            Log.w(TAG, "Consent flow already running — ignoring duplicate call.")
            onComplete(false)
            return
        }

        isProcessingConsent = true

        try {
            val params = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()

            consentInformation = UserMessagingPlatform.getConsentInformation(activity)

            consentInformation?.requestConsentInfoUpdate(
                activity,
                params,
                {
                    showConsentFormIfNeeded(activity, onComplete)
                },
                { error ->
                    Log.e(TAG, "Consent info update failed: ${error.message}")
                    finishConsent(activity, granted = false, onComplete)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting consent info: ${e.message}")
            finishConsent(activity, granted = false, onComplete)
        }
    }

    // 🪟 Load and show form if needed
    private fun showConsentFormIfNeeded(activity: Activity, onComplete: (Boolean) -> Unit) {
        try {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                formError?.let {
                    Log.w(TAG, "Consent form error: ${it.message}")
                }

                val canRequestAds = consentInformation?.canRequestAds() ?: false
                val consentStatus = consentInformation?.consentStatus

                logD(TAG, "Consent status: $consentStatus, canRequestAds=$canRequestAds")

                when (consentStatus) {
                    ConsentInformation.ConsentStatus.OBTAINED -> finishConsent(activity, true, onComplete)
                    ConsentInformation.ConsentStatus.NOT_REQUIRED -> finishConsent(activity, true, onComplete)
                    else -> finishConsent(activity, false, onComplete)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing consent form: ${e.message}")
            finishConsent(activity, false, onComplete)
        }
    }

    // ✅ Apply results, update mediation SDKs & Firebase
    private fun finishConsent(activity: Activity, granted: Boolean, onComplete: (Boolean) -> Unit) {
        logD(TAG, "Finishing consent: granted=$granted")

        // 🔹 1. Initialize AdMob SDK
        if (granted) initMobileAds(activity.applicationContext)

        // 🔹 2. Update mediation partners
        updateMediationSDKs(activity, granted)

        // 🔹 3. Update Firebase Analytics consent
        updateFirebaseConsent(activity, granted)

        // 🔹 4. Callback
        onComplete(granted)
        isProcessingConsent = false
    }

    // 📡 Initialize AdMob only once
    private fun initMobileAds(context: Context) {
        if (isMobileAdsInitialized) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(context)
                isMobileAdsInitialized = true
                logD(TAG, "MobileAds initialized")
            } catch (e: Exception) {
                Log.e(TAG, "MobileAds init failed: ${e.message}")
            }
        }
    }

    // 🧩 Update Firebase Analytics consent
    private fun updateFirebaseConsent(context: Context, granted: Boolean) {
        try {
//            val map = EnumMap(FirebaseAnalytics.ConsentType::class.java)

            val map: MutableMap<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus> =
                EnumMap(FirebaseAnalytics.ConsentType::class.java)

            val status = if (granted) FirebaseAnalytics.ConsentStatus.GRANTED
            else FirebaseAnalytics.ConsentStatus.DENIED

            map[FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE] = status
            map[FirebaseAnalytics.ConsentType.AD_STORAGE] = status
            map[FirebaseAnalytics.ConsentType.AD_USER_DATA] = status
            map[FirebaseAnalytics.ConsentType.AD_PERSONALIZATION] = status

            FirebaseAnalytics.getInstance(context).setConsent(map)
            logD(TAG, "Firebase consent set to: $status")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update Firebase consent: ${e.message}")
        }
    }

    // 🔗 Update mediation SDKs with GDPR/consent info
    private fun updateMediationSDKs(activity: Activity, granted: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // AppLovin


                // Vungle


                // MBridge


                // InMobi

                logD(TAG, "Mediation SDKs updated with consent=$granted")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating mediation SDKs: ${e.message}")
            }
        }
    }

    // ⚙️ Helper: can we show ads?
    fun canRequestAds(): Boolean {
        return consentInformation?.canRequestAds() ?: false
    }

    // ⚙️ Helper: show privacy options again
    fun showPrivacyOptionsForm(activity: Activity, onDismiss: (FormError?) -> Unit) {
        try {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) {
                onDismiss(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing privacy options: ${e.message}")
            onDismiss(null)
        }
    }
}*/
