package com.hypnotixstudio.electroxbd

import android.app.Activity
import android.util.Log
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 *  ADMOB.KT — AdMob Manager
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 *  This class handles all AdMob ad operations:
 *    - SDK initialization
 *    - Banner ad loading & display
 *    - Interstitial ad preloading, display, and interval management
 *
 *  SAFETY: Every method checks if the corresponding Ad Unit ID is provided
 *  in Config.kt before doing anything. If IDs are empty, all methods become
 *  silent no-ops — no crashes, no errors, no exceptions.
 *
 *  DO NOT MODIFY THIS FILE unless you need to add advanced ad features.
 *  All configuration is done in Config.kt.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
class Admob {

    companion object {
        private const val TAG = "AdmobManager"
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  INTERNAL STATE
    // ═════════════════════════════════════════════════════════════════════════

    /** The currently loaded interstitial ad (null if not yet loaded or already shown). */
    private var interstitialAd: InterstitialAd? = null

    /** Timestamp (in millis) of the last time an interstitial was displayed. */
    private var lastInterstitialTime: Long = 0L

    /** Whether the Mobile Ads SDK has been initialized. */
    private var isInitialized: Boolean = false

    // ═════════════════════════════════════════════════════════════════════════
    //  SDK INITIALIZATION
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Initializes the Google Mobile Ads SDK.
     *
     * This must be called once in onCreate() before loading any ads.
     * If [Config.ADMOB_APP_ID] is blank, this method does nothing.
     *
     * @param activity The hosting Activity context.
     */
    fun init(activity: Activity) {
        // Safety check: skip initialization if AdMob is disabled
        if (!Config.isAdmobEnabled()) {
            Log.d(TAG, "AdMob disabled — ADMOB_APP_ID is empty. Skipping SDK initialization.")
            return
        }

        MobileAds.initialize(activity) { initializationStatus ->
            isInitialized = true
            Log.d(TAG, "AdMob SDK initialized. Status: ${initializationStatus.adapterStatusMap}")
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BANNER AD
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Sets up and loads a banner ad into the provided [AdView].
     *
     * If [Config.ADMOB_BANNER_ID] is blank, the [AdView] is hidden (GONE)
     * and no ad request is made.
     *
     * @param adView The AdView from the layout to load the banner into.
     */
    fun setupBanner(adView: AdView) {
        // Safety check: hide the banner container if banner ads are disabled
        if (!Config.isBannerEnabled()) {
            adView.visibility = View.GONE
            Log.d(TAG, "Banner disabled — ADMOB_BANNER_ID is empty. AdView hidden.")
            return
        }

        // Make the banner container visible and load the ad
        adView.visibility = View.VISIBLE
        adView.adUnitId = Config.ADMOB_BANNER_ID

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        Log.d(TAG, "Banner ad request sent for unit: ${Config.ADMOB_BANNER_ID}")
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  INTERSTITIAL AD
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Preloads an interstitial ad so it's ready to show immediately when needed.
     *
     * If [Config.ADMOB_INTERSTITIAL_ID] is blank, this method does nothing.
     * Call this in onCreate() and again after each time an interstitial is displayed.
     *
     * @param activity The hosting Activity context.
     */
    fun loadInterstitial(activity: Activity) {
        // Safety check: skip if interstitial ads are disabled
        if (!Config.isInterstitialEnabled()) {
            Log.d(TAG, "Interstitial disabled — ADMOB_INTERSTITIAL_ID is empty. Skipping preload.")
            return
        }

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity,
            Config.ADMOB_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial ad loaded successfully.")

                    // Set up the full-screen content callback to handle ad lifecycle
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            // Ad was closed by the user — preload the next one
                            Log.d(TAG, "Interstitial dismissed. Preloading next ad.")
                            interstitialAd = null
                            loadInterstitial(activity)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                            Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                            interstitialAd = null
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Interstitial failed to load: ${loadAdError.message}")
                    interstitialAd = null
                }
            }
        )
    }

    /**
     * Shows the interstitial ad if ALL of the following conditions are met:
     *   1. Interstitial ads are enabled in Config.kt
     *   2. An interstitial ad has been successfully preloaded
     *   3. Enough time has elapsed since the last interstitial was shown
     *      (controlled by [Config.INTERSTITIAL_INTERVAL_SECONDS])
     *
     * @param activity The hosting Activity to display the ad on.
     * @param onDismiss Optional callback to execute when the ad is dismissed.
     * @return true if the interstitial was shown, false otherwise.
     */
    fun showInterstitialIfReady(activity: Activity, onDismiss: (() -> Unit)? = null): Boolean {
        // Safety check: skip if interstitial ads are disabled
        if (!Config.isInterstitialEnabled()) return false

        // Check if an ad is loaded
        val ad = interstitialAd ?: return false

        // Check if enough time has elapsed since the last display
        val now = System.currentTimeMillis()
        val intervalMillis = Config.INTERSTITIAL_INTERVAL_SECONDS * 1000L
        if (now - lastInterstitialTime < intervalMillis) {
            Log.d(TAG, "Interstitial interval not met. ${(intervalMillis - (now - lastInterstitialTime)) / 1000}s remaining.")
            return false
        }

        // All conditions met — show the interstitial
        lastInterstitialTime = now

        // Override callback if onDismiss is provided
        if (onDismiss != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial dismissed (with callback).")
                    interstitialAd = null
                    onDismiss()
                    loadInterstitial(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                    interstitialAd = null
                    onDismiss()
                }
            }
        }

        ad.show(activity)
        Log.d(TAG, "Interstitial ad displayed.")
        return true
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CLEANUP
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Cleans up ad resources. Call this in onDestroy().
     *
     * @param adView The banner AdView to destroy (nullable for safety).
     */
    fun destroy(adView: AdView?) {
        adView?.destroy()
        interstitialAd = null
        Log.d(TAG, "Ad resources cleaned up.")
    }
}
