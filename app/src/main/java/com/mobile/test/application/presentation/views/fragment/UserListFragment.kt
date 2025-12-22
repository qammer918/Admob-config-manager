package com.mobile.test.application.presentation.views.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ads.adsmodule.ads.bannerad.BannerAdManager
import com.ads.adsmodule.ads.bannerad.BannerSlot
import com.ads.adsmodule.ads.interstitial.InterstitialAdHelper
import com.ads.adsmodule.ads.interstitial.InterstitialSlot
import com.ads.adsmodule.ads.nativeAd.NativeAdManager
import com.ads.adsmodule.ads.nativeAd.NativeAdSlot
import com.ads.adsmodule.ads.open_app.AppOpenSlot
import com.mobile.test.application.R
import com.mobile.test.application.app.MyApplication
import com.mobile.test.application.core.beGone
import com.mobile.test.application.core.beVisible
import com.mobile.test.application.core.singleClick
import com.mobile.test.application.core.snackBar
import com.mobile.test.application.databinding.FragmentUserListBinding
import com.mobile.test.application.presentation.adapter.UserAdapter
import com.mobile.test.application.presentation.viewmodel.UserViewModel
import com.module.remoteconfig.utils.Constants.appOpenInAppId
import com.module.remoteconfig.utils.Constants.bannerSplashId
import com.module.remoteconfig.utils.Constants.interstitialFunctionId
import com.module.remoteconfig.utils.Constants.nativeOnBoardingId
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserListFragment : BaseFragment<FragmentUserListBinding>(FragmentUserListBinding::inflate) {
    private val viewModel: UserViewModel by viewModels()
    private lateinit var userAdapter: UserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        observeViewModel()

        val application = activity?.applicationContext as? MyApplication
        application?.appOpenAdManager?.fetchAd(AppOpenSlot.MAIN,appOpenInAppId)

        InterstitialAdHelper.setAdCallback { slot, status ->
            Log.d("AdStatus", "$slot → $status")
        }


        InterstitialAdHelper.preloadAd(
            requireActivity(),
            InterstitialSlot.HOME,
            interstitialFunctionId
        )

        lifecycleScope.launch {
            loadAndShowBanner()

        }


//        MultiInterstitialAdPreloadManager.preloadAds(requireContext(), adIds)


//
//        NativeAdManager.loadNativeAd(
//            slot = NativeAdSlot.SETTINGS,
//            activity = requireActivity(),
//            adLayoutId = R.layout.native_language_layout,
//            adContainer = null, // preload silently
//            adUnitId = "ca-app-pub-3940256099942544/2247696110",
//            shimmerView = null
//        )


        NativeAdManager.loadNativeAd(
            slot = NativeAdSlot.HOME,
            activity = requireActivity(),
            adLayoutId = R.layout.native_language_layout,
            adContainer = binding.frameInlineNative,
            adUnitId = nativeOnBoardingId,
            shimmerView = binding.nativeInline2Shimmer.root,
            showMedia = true
        )


    }


    private fun loadAndShowBanner() {
        BannerAdManager.loadBanner(
            activity = requireActivity(),
            container = binding.adContainer,
            slot = BannerSlot.SETTINGS,
            adUnitId = bannerSplashId,
            shimmerView = binding.shimmerLayout.root
        ) { slot, status ->
            Log.d("BannerAd", "[$slot] Status: $status")
        }
    }


    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            onEditClick = { user ->
                navigateToUserForm(user.id)
            },
            onDeleteClick = { user ->
                viewModel.deleteUser(user)
            }
        )

        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onSearchQueryChanged(s.toString())
            }
        })
    }

    private fun setupClickListeners() {
        binding.fabAddUser.singleClick {
            navigateToUserForm()
//            lifecycleScope.launch {
//                InterstitialAdHelper.showPreloadedAd(requireActivity(), InterstitialSlot.HOME) {
//                    navigateToUserForm()
//                }
//            }

        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE

                if (uiState.users.isEmpty() && !uiState.isLoading) {
                    binding.tvEmpty.beVisible()
                    binding.rvUsers.beGone()
                } else {
                    binding.tvEmpty.beGone()
                    binding.rvUsers.beVisible()
                    userAdapter.submitList(uiState.users)
                }

                uiState.error?.let { error ->
                    // Handle error - you can show a snackbar or toast
                    binding.root.snackBar(error)
                }
            }
        }
    }

    private fun navigateToUserForm(userId: Long = -1) {
        val bundle = Bundle().apply {
            putLong("userId", userId)
        }
        findNavController().navigate(R.id.action_userList_to_userForm, bundle)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        NativeAdManager.destroyAd(NativeAdSlot.HOME)
        InterstitialAdHelper.destroy(InterstitialSlot.EXIT)
        BannerAdManager.destroyBanner(BannerSlot.SETTINGS)
    }

}

