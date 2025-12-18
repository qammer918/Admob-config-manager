package com.mobile.test.application.presentation.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor( @ApplicationContext private val context: Context) : ViewModel(),
    PurchasesUpdatedListener,
    ProductDetailsResponseListener {
    private var retriesRemaining = 5

    private val _purchases = MutableLiveData<List<Purchase>?>()
    private val _productWithProductDetails = MutableLiveData<Map<String, ProductDetails>>()
    private val _isNewPurchaseAcknowledged = MutableLiveData<Boolean>()
    private val _isBillingConnected = MutableLiveData<Boolean>()

    fun currentPurchases(): LiveData<List<Purchase>?> = _purchases
    fun isNewPurchaseAcknowledged(): LiveData<Boolean> = _isNewPurchaseAcknowledged
    fun isBillingConnected(): LiveData<Boolean> = _isBillingConnected

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    // Establish a connection to Google Play.
    fun startBillingConnection() {
        retriesRemaining = 3
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryAllPurchases()
                    queryAllProductDetails()
                    _isBillingConnected.postValue(true)
                }
            }

            override fun onBillingServiceDisconnected() {
                if (retriesRemaining > 0) {
                    retriesRemaining--
                }
                _isBillingConnected.postValue(false)
            }
        })
    }

    fun monthlyProductDetails(): LiveData<Map<String, ProductDetails>> = _productWithProductDetails


    // Query Google Play Billing for existing purchases.
    fun queryAllPurchases() {
        queryPurchases(BillingProductType.SUBS)
        queryPurchases(BillingProductType.IN_APP)
    }

    private fun queryPurchases(type: BillingProductType) {
        if (!billingClient.isReady) return

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(type.type)
                .build()
        ) { billingResult, purchaseList ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val validPurchases = purchaseList.filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                validPurchases.forEach {
                    if (!it.isAcknowledged) acknowledgePurchases(it)
                }

                _purchases.postValue(validPurchases)
            }
        }
    }

    // Query Google Play Billing for products available to sell and present them in the UI
    fun queryAllProductDetails() {
        queryProductDetails(BillingProductType.SUBS, SUB_PRODUCTS)
        queryProductDetails(BillingProductType.IN_APP, IN_APP_PRODUCTS)
    }

    private fun queryProductDetails(
        type: BillingProductType,
        products: List<String>
    ) {
        val productList = products.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(type.type)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val map = list.associateBy { it.productId }
                _productWithProductDetails.postValue(map)
            }
        }
    }

    // Handle product details response
    override fun onProductDetailsResponse(billingResult: BillingResult, productDetailsList: MutableList<ProductDetails>) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
            val productDetailsMap = productDetailsList.associateBy { it.productId }
            _productWithProductDetails.postValue(productDetailsMap)
        }
    }



    //    // Launch Purchase flow
    fun buySubscription(productDetails: ProductDetails, activity: Activity, offerType: String) {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull {
            it.pricingPhases.pricingPhaseList[0].billingPeriod == offerType
        }?.offerToken

        val billingParams = offerToken?.let {
            billingFlowParamsBuilder(productDetails, it)
        }

        billingParams?.let {
            launchBillingFlow(activity, it.build())
        }
    }


    fun buyInApp(
        activity: Activity,
        productDetails: ProductDetails
    ) {
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        billingClient.launchBillingFlow(activity, params)
    }


    // Helper to build the billing flow params
    private fun billingFlowParamsBuilder(productDetails: ProductDetails, offerToken: String): BillingFlowParams.Builder {
        return BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
        )
    }


    // Launch Purchase flow
    private fun launchBillingFlow(activity: Activity, params: BillingFlowParams) {
        if (billingClient.isReady) {

            billingClient.launchBillingFlow(activity, params)
        }

    }

    // Acknowledge new purchases
    private fun acknowledgePurchases(purchase: Purchase?) {
        purchase?.let {
            if (!it.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(it.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && it.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        _isNewPurchaseAcknowledged.postValue(true)
                    }
                }
            }
        }
    }

    // End Billing connection
    private fun terminateBillingConnection() {
        billingClient.endConnection()
    }

    override fun onCleared() {
        terminateBillingConnection()
        super.onCleared()
    }

    companion object {
        const val YEARLY_FT = "yearly_freetrial"

        // IN_APP
        const val REMOVE_ADS = "remove_ads"
        val SUB_PRODUCTS = listOf(YEARLY_FT)
        val IN_APP_PRODUCTS = listOf(REMOVE_ADS)

    }
    enum class BillingProductType(val type: String) {
        IN_APP(BillingClient.ProductType.INAPP),
        SUBS(BillingClient.ProductType.SUBS)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
            && !purchases.isNullOrEmpty()
        ) {
//            subscriptionPurchased?.invoke(true)
            // Post new purchase List to _purchases
            _purchases.postValue(purchases)
            // Then, handle the purchases
            for (purchase in purchases) {
                acknowledgePurchases(purchase)
            }

        }
    }
}
