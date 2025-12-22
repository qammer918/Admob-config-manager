package com.mobile.test.application.presentation.views.fragment

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ads.adsmodule.ads.bannerad.BannerAdManager
import com.ads.adsmodule.ads.bannerad.BannerSlot
import com.ads.adsmodule.ads.interstitial.InterstitialAdHelper
import com.ads.adsmodule.ads.interstitial.InterstitialSlot
import com.ads.adsmodule.ads.nativeAd.NativeAdManager
import com.ads.adsmodule.ads.nativeAd.NativeAdSlot
import com.mobile.test.application.R
import com.mobile.test.application.core.click
import com.mobile.test.application.core.singleClick
import com.mobile.test.application.databinding.FragmentUserFormBinding
import com.mobile.test.application.domain.model.User
import com.mobile.test.application.presentation.viewmodel.UserViewModel
import com.module.remoteconfig.utils.Constants.bannerSplashId
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserFormFragment : BaseFragment<FragmentUserFormBinding>(FragmentUserFormBinding::inflate) {

    private val viewModel: UserViewModel by viewModels()
    private val userId: Long by lazy {
        arguments?.getLong("userId", -1L) ?: -1L
    }

    private var currentUser: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        loadUserIfEditing()

        addBackPressListener()

        NativeAdManager.loadNativeAd(
            slot = NativeAdSlot.SETTINGS,
            activity = requireActivity(),
            adLayoutId = R.layout.native_language_layout,
            adContainer = binding.frameInlineNative,
            adUnitId = "ca-app-pub-3940256099942544/2247696110",
            shimmerView = binding.nativeInline2Shimmer.root,
            showMedia = true
        )


        NativeAdManager.loadNativeAd(
            slot = NativeAdSlot.ABOUT,
            activity = requireActivity(),
            adLayoutId = R.layout.native_language_layout,
            adContainer = binding.frameInlineNative2,
            adUnitId = "ca-app-pub-3940256099942544/2247696110",
            shimmerView = binding.nativeInline2Shimmer2.root,
            showMedia = false
        )


        NativeAdManager.loadNativeAd(
            slot = NativeAdSlot.MENU,
            activity = requireActivity(),
            adLayoutId = R.layout.native_language_layout,
            adContainer = binding.frameInlineNative3,
            adUnitId = "ca-app-pub-3940256099942544/2247696110",
            shimmerView = binding.nativeInline2Shimmer3.root,
            showMedia = true
        )


        NativeAdManager.loadNativeAd(
            slot = NativeAdSlot.NEW,
            activity = requireActivity(),
            adLayoutId = R.layout.native_language_layout,
            adContainer = binding.frameInlineNative4,
            adUnitId = "ca-app-pub-3940256099942544/2247696110",
            shimmerView = binding.nativeInline2Shimmer4.root,
            showMedia = true
        )


        NativeAdManager.loadNativeAd(
            slot = NativeAdSlot.NEW2,
            activity = requireActivity(),
            adLayoutId = R.layout.native_language_layout,
            adContainer = binding.frameInlineNative5,
            adUnitId = "ca-app-pub-3940256099942544/2247696110",
            shimmerView = binding.nativeInline2Shimmer5.root,
            showMedia = true
        )

        loadAndShowBanner()




    }



    private fun loadAndShowBanner() {
        BannerAdManager.loadBanner(
            activity = requireActivity(),
            container = binding.adContainer,
            slot = BannerSlot.CUSTOM,
            adUnitId = bannerSplashId,
            binding.shimmerLayout.root
        ) { slot, status ->
            Log.d("BannerAd", "[$slot] Status: $status")
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnSave.singleClick {
                saveUser()
            }

            btnCancel click {
                findNavController().navigateUp()
            }

        }
    }

    private fun loadUserIfEditing() {
        if (userId != -1L) {
            viewLifecycleOwner.lifecycleScope.launch {
                currentUser = viewModel.getUserById(userId)
                currentUser?.let { user ->
                    binding.apply {
                        etName.setText(user.name)
                        etEmail.setText(user.email)
                        etPhone.setText(user.phone)
                        etAge.setText(user.age.toString())
                    }
                }
            }
        }
    }

    private fun saveUser() {
        binding.apply {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val ageText = etAge.text.toString().trim()
            if (validateInput(name, email, phone, ageText)) {
                val age = ageText.toInt()
                val user = User(
                    id = currentUser?.id ?: 0,
                    name = name,
                    email = email,
                    phone = phone,
                    age = age
                )

                if (currentUser != null) {
                    viewModel.updateUser(user)
                } else {
                    viewModel.insertUser(user)
                }

                findNavController().navigateUp()
            }
        }
    }


    private fun addBackPressListener() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {





                    InterstitialAdHelper.showOnDemandAd(
                        activity = requireActivity(),
                        slot = InterstitialSlot.EXIT,
                        adId = "ca-app-pub-3940256099942544/1033173712",
                    ) {
                        findNavController().navigateUp()
                        Log.d("Ad", "Exit ad dismissed")
                    }


                }
            }
        )
    }


    private fun validateInput(
        name: String,
        email: String,
        phone: String,
        ageText: String
    ): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            isValid = false
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Valid email is required"
            isValid = false
        }

        if (phone.isEmpty()) {
            binding.etPhone.error = "Phone is required"
            isValid = false
        }

        if (ageText.isEmpty()) {
            binding.etAge.error = "Age is required"
            isValid = false
        } else {
            try {
                val age = ageText.toInt()
                if (age <= 0 || age > 150) {
                    binding.etAge.error = "Age must be between 1 and 150"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                binding.etAge.error = "Invalid age"
                isValid = false
            }
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()

        NativeAdManager.destroyAd(NativeAdSlot.SETTINGS)
        NativeAdManager.destroyAd(NativeAdSlot.NEW)
        NativeAdManager.destroyAd(NativeAdSlot.NEW2)
        NativeAdManager.destroyAd(NativeAdSlot.MENU)
        NativeAdManager.destroyAd(NativeAdSlot.ABOUT)

    }


}
