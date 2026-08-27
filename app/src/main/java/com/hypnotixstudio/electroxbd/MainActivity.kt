package com.hypnotixstudio.electroxbd

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.hypnotixstudio.electroxbd.databinding.ActivityMainBinding

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 *  MAINACTIVITY.KT — ElectroXBD WebView Activity
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 *  This is the main (and only) Activity of the app. It handles:
 *    - WebView setup and content loading (URL or LOCAL mode via Config.kt)
 *    - Chrome-style thin progress bar at the top of the screen
 *    - Pull-to-refresh via SwipeRefreshLayout
 *    - AdMob banner and interstitial ad integration via Admob.kt
 *    - Hardware back button navigation within the WebView
 *
 *  DO NOT MODIFY THIS FILE for basic configuration.
 *  All user-facing settings are in Config.kt.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
class MainActivity : AppCompatActivity() {

    // ═════════════════════════════════════════════════════════════════════════
    //  PROPERTIES
    // ═════════════════════════════════════════════════════════════════════════

    /** View Binding for type-safe access to all layout views. */
    private lateinit var binding: ActivityMainBinding

    /** AdMob manager instance for banner and interstitial ads. */
    private val admob = Admob()

    // ═════════════════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ═════════════════════════════════════════════════════════════════════════

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ─────────────────────────────────────────────────────────────────
        // 1. INFLATE LAYOUT via View Binding
        // ─────────────────────────────────────────────────────────────────
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ─────────────────────────────────────────────────────────────────
        // 2. CONFIGURE CHROME-STYLE PROGRESS BAR
        //    Applies the color defined in Config.PROGRESS_BAR_COLOR
        // ─────────────────────────────────────────────────────────────────
        try {
            val progressColor = Color.parseColor(Config.PROGRESS_BAR_COLOR)
            binding.progressBar.progressTintList = ColorStateList.valueOf(progressColor)
        } catch (e: IllegalArgumentException) {
            // Fallback to Google Blue if the user provides an invalid hex color
            binding.progressBar.progressTintList = ColorStateList.valueOf(Color.parseColor("#4285F4"))
        }

        // ─────────────────────────────────────────────────────────────────
        // 3. CONFIGURE SWIPE-TO-REFRESH
        //    Applies the spinner colors defined in Config.SWIPE_REFRESH_COLORS
        // ─────────────────────────────────────────────────────────────────
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.webView.reload()
        }

        // Parse color hex strings to integers for the refresh spinner
        try {
            val colors = Config.SWIPE_REFRESH_COLORS.map { Color.parseColor(it) }.toIntArray()
            binding.swipeRefreshLayout.setColorSchemeColors(*colors)
        } catch (e: IllegalArgumentException) {
            // Fallback to Google Blue if any color is invalid
            binding.swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#4285F4"))
        }

        // ─────────────────────────────────────────────────────────────────
        // 4. CONFIGURE WEBVIEW
        // ─────────────────────────────────────────────────────────────────
        binding.webView.apply {
            // Enable JavaScript (required for most modern websites)
            settings.javaScriptEnabled = true

            // Enable DOM storage (localStorage / sessionStorage)
            settings.domStorageEnabled = true

            // Allow mixed content (HTTPS pages loading HTTP resources)
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            // Enable zoom controls (pinch-to-zoom)
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false // Hide the +/- zoom buttons

            // Allow file access from file:// URLs (needed for LOCAL mode)
            settings.allowFileAccess = true

            // Enable wide viewport and load pages in overview mode
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true

            // Set a modern user agent substring
            settings.userAgentString = settings.userAgentString

            // ─────────────────────────────────────────────────────────────
            // 4a. WebViewClient — Keeps navigation inside the app
            // ─────────────────────────────────────────────────────────────
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    // Return false to let the WebView handle all URLs internally
                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    // Page started loading — show the progress bar
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.progress = 0
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Page finished loading — hide the progress bar and stop refresh spinner
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }

            // ─────────────────────────────────────────────────────────────
            // 4b. WebChromeClient — Drives the progress bar animation
            // ─────────────────────────────────────────────────────────────
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    // Update the progress bar with the current loading percentage (0–100)
                    binding.progressBar.progress = newProgress

                    if (newProgress >= 100) {
                        // Fully loaded — hide the progress bar
                        binding.progressBar.visibility = View.GONE
                    } else {
                        // Still loading — ensure progress bar is visible
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────────────
        // 5. LOAD CONTENT based on APP_MODE in Config.kt
        // ─────────────────────────────────────────────────────────────────
        if (savedInstanceState == null) {
            when {
                Config.isUrlMode() -> {
                    // Mode 1: Load a live website from the internet
                    binding.webView.loadUrl(Config.WEBSITE_URL)
                }
                Config.isLocalMode() -> {
                    // Mode 2: Load local HTML/CSS/JS from assets/www/ folder
                    binding.webView.loadUrl(Config.getLocalUrl())
                }
                else -> {
                    // Fallback: If APP_MODE is invalid, load the URL as a safe default
                    binding.webView.loadUrl(Config.WEBSITE_URL)
                }
            }
        } else {
            // Restore WebView state on configuration change / process recreation
            binding.webView.restoreState(savedInstanceState)
        }

        // ─────────────────────────────────────────────────────────────────
        // 6. INITIALIZE ADS (only if enabled in Config.kt)
        // ─────────────────────────────────────────────────────────────────
        admob.init(this)
        admob.setupBanner(binding.adView)
        admob.loadInterstitial(this)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  STATE MANAGEMENT
    // ═════════════════════════════════════════════════════════════════════════

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save WebView state so it survives process recreation
        binding.webView.saveState(outState)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BACK BUTTON HANDLING
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Handles the hardware/software back button press.
     *
     * Behavior:
     *   - If the WebView has browsing history, navigate back within it.
     *   - If at the first page, show an interstitial ad (if interval allows),
     *     then exit the app.
     */
    @Suppress("DEPRECATION")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (binding.webView.canGoBack()) {
                // WebView has history — go back one page
                binding.webView.goBack()
                return true
            } else {
                // At the root page — try to show an interstitial before exiting
                if (admob.showInterstitialIfReady(this)) {
                    // Interstitial shown — app will exit after the user closes the ad
                    // (handled in the fullScreenContentCallback dismiss)
                    return true
                }
                // No interstitial shown — proceed with default back behavior (exit)
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LIFECYCLE CALLBACKS
    // ═════════════════════════════════════════════════════════════════════════

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onPause() {
        binding.webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        // Clean up WebView and ad resources to prevent memory leaks
        admob.destroy(binding.adView)
        binding.webView.destroy()
        super.onDestroy()
    }
}