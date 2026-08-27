package com.hypnotixstudio.electroxbd

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
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
 *    - No Internet Connection detection and custom error page
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

    /** Connectivity manager for monitoring network state changes. */
    private lateinit var connectivityManager: ConnectivityManager

    /** Network callback to detect connectivity changes in real-time. */
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    /** Tracks whether the no-internet page is currently visible. */
    private var isNoInternetShowing = false

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

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    // Only handle main frame errors (not sub-resource errors)
                    if (request?.isForMainFrame == true) {
                        val errorCode = error?.errorCode ?: return
                        // Show no-internet page for connectivity-related errors
                        if (errorCode == ERROR_HOST_LOOKUP ||
                            errorCode == ERROR_CONNECT ||
                            errorCode == ERROR_TIMEOUT ||
                            errorCode == ERROR_IO
                        ) {
                            showNoInternet()
                        }
                    }
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
            if (isNetworkAvailable()) {
                loadContent()
            } else {
                showNoInternet()
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

        // ─────────────────────────────────────────────────────────────────
        // 7. SETUP NO-INTERNET BUTTONS & NETWORK MONITORING
        // ─────────────────────────────────────────────────────────────────
        setupNoInternetButtons()
        setupNetworkMonitor()

        // ─────────────────────────────────────────────────────────────────
        // 8. SETUP MODERN BACK NAVIGATION HANDLING
        //    Handles both hardware back button and gesture navigation
        // ─────────────────────────────────────────────────────────────────
        setupBackNavigation()
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BACK NAVIGATION HANDLING
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Sets up the modern OnBackPressedCallback to handle system back navigation
     * (hardware buttons and gestures) for both WebView history and exit confirmation.
     */
    private fun setupBackNavigation() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    // 1. If WebView can go back, navigate back in history
                    binding.webView.canGoBack() -> {
                        binding.webView.goBack()
                    }
                    // 2. If at the root, show exit confirmation dialog
                    else -> {
                        showExitConfirmation()
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  NO INTERNET — UI & ANIMATIONS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Shows the no-internet overlay with entrance animations.
     * Hides the WebView and SwipeRefreshLayout behind the overlay.
     */
    private fun showNoInternet() {
        if (isNoInternetShowing) return
        isNoInternetShowing = true

        runOnUiThread {
            // Show the overlay
            binding.noInternetLayout.visibility = View.VISIBLE

            // Hide webview content behind overlay
            binding.swipeRefreshLayout.visibility = View.GONE
            binding.progressBar.visibility = View.GONE

            // ── Animate the icon container (bounce scale) ──
            val iconAnim = AnimationUtils.loadAnimation(this, R.anim.scale_bounce_in)
            binding.iconContainer.startAnimation(iconAnim)

            // ── Animate title and subtitle (slide up + fade) ──
            val slideUpAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
            slideUpAnim.startOffset = 200
            binding.noInternetTitle.startAnimation(slideUpAnim)

            val subtitleAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
            subtitleAnim.startOffset = 350
            binding.noInternetSubtitle.startAnimation(subtitleAnim)

            // ── Animate buttons (staggered slide up) ──
            val btn1Anim = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
            btn1Anim.startOffset = 500
            binding.btnConnectWifi.startAnimation(btn1Anim)

            val btn2Anim = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
            btn2Anim.startOffset = 600
            binding.btnMobileData.startAnimation(btn2Anim)

            val btn3Anim = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
            btn3Anim.startOffset = 700
            binding.btnRetry.startAnimation(btn3Anim)
        }
    }

    /**
     * Hides the no-internet overlay and re-shows the WebView content.
     */
    private fun hideNoInternet() {
        if (!isNoInternetShowing) return
        isNoInternetShowing = false

        runOnUiThread {
            // Fade out the overlay
            binding.noInternetLayout.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.noInternetLayout.visibility = View.GONE
                    binding.noInternetLayout.alpha = 1f // Reset for next show
                    binding.swipeRefreshLayout.visibility = View.VISIBLE
                }
                .start()
        }
    }


    // ═════════════════════════════════════════════════════════════════════════
    //  NO INTERNET — BUTTON HANDLERS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Shows a confirmation dialog before exiting the app.
     */
    private fun showExitConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.exit_title)
            .setMessage(R.string.exit_message)
            .setPositiveButton(R.string.exit_yes) { _, _ ->
                if (!admob.showInterstitialIfReady(this) { finish() }) {
                    finish()
                }
            }
            .setNegativeButton(R.string.exit_no, null)
            .show()
    }

    /**
     * Wires up the three action buttons on the no-internet overlay:
     *   - "Turn On Wi-Fi" → Opens system Wi-Fi settings
     *   - "Turn On Mobile Data" → Opens system wireless/data settings
     *   - "Try Again" → Re-checks connectivity and reloads if online
     */
    private fun setupNoInternetButtons() {
        // Open Wi-Fi settings
        binding.btnConnectWifi.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        // Open Mobile Data / Wireless settings
        binding.btnMobileData.setOnClickListener {
            startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))
        }

        // Retry: check connectivity and reload
        binding.btnRetry.setOnClickListener {
            if (isNetworkAvailable()) {
                hideNoInternet()
                loadContent()
            } else {
                // Shake the icon to indicate still no connection
                binding.iconContainer.animate()
                    .translationX(-12f).setDuration(60)
                    .withEndAction {
                        binding.iconContainer.animate()
                            .translationX(12f).setDuration(60)
                            .withEndAction {
                                binding.iconContainer.animate()
                                    .translationX(-8f).setDuration(60)
                                    .withEndAction {
                                        binding.iconContainer.animate()
                                            .translationX(8f).setDuration(60)
                                            .withEndAction {
                                                binding.iconContainer.animate()
                                                    .translationX(0f).setDuration(60)
                                                    .start()
                                            }.start()
                                    }.start()
                            }.start()
                    }.start()
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  NETWORK MONITORING
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Registers a real-time network callback that automatically:
     *   - Shows the no-internet overlay when connectivity is lost
     *   - Hides the overlay and reloads when connectivity is restored
     */
    private fun setupNetworkMonitor() {
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Network is back — hide the overlay and reload
                if (isNoInternetShowing) {
                    runOnUiThread {
                        hideNoInternet()
                        loadContent()
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // Network lost — only show overlay if in URL mode (local mode doesn't need internet)
                if (Config.isUrlMode()) {
                    runOnUiThread {
                        showNoInternet()
                    }
                }
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    /**
     * Checks if the device currently has an active internet-capable connection.
     */
    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Loads content based on the configured APP_MODE (URL or LOCAL).
     */
    private fun loadContent() {
        when {
            Config.isUrlMode() -> {
                binding.webView.loadUrl(Config.WEBSITE_URL)
            }
            Config.isLocalMode() -> {
                binding.webView.loadUrl(Config.getLocalUrl())
            }
            else -> {
                binding.webView.loadUrl(Config.WEBSITE_URL)
            }
        }
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
    //  LIFECYCLE CALLBACKS
    // ═════════════════════════════════════════════════════════════════════════

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()

        // Re-check connectivity when the user returns from Wi-Fi/Data settings
        if (isNoInternetShowing && isNetworkAvailable()) {
            hideNoInternet()
            loadContent()
        }
    }

    override fun onPause() {
        binding.webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        // Unregister network callback to prevent leaks
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) { }

        // Clean up WebView and ad resources to prevent memory leaks
        admob.destroy(binding.adView)
        binding.webView.destroy()
        super.onDestroy()
    }
}