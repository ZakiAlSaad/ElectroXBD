package com.hypnotixstudio.electroxbd

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 *  CONFIG.KT — ElectroXBD Configuration Center
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 *  This is the ONLY file you need to edit to configure your app.
 *  Every constant below has a detailed comment explaining what it does,
 *  what values are accepted, and what happens when you leave it blank.
 *
 *  QUICK START:
 *    1. Choose your mode: "URL" to load a website, "LOCAL" to load from assets/
 *    2. Set the appropriate URL or local file path
 *    3. (Optional) Add your AdMob IDs to enable ads
 *    4. Build & Run — that's it!
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
object Config {

    // ═════════════════════════════════════════════════════════════════════════
    //  APP LOADING MODE
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * APP_MODE — Controls how the app loads its content.
     *
     * Accepted values:
     *   "URL"   → Mode 1: Loads a live website from the internet via [WEBSITE_URL]
     *   "LOCAL" → Mode 2: Loads local HTML/CSS/JS files from the assets/www/ folder
     *
     * ⚠ This value is case-sensitive. Use exactly "URL" or "LOCAL".
     *
     * Example:
     *   const val APP_MODE = "URL"    // Load a live website
     *   const val APP_MODE = "LOCAL"  // Load local HTML files from assets/www/
     */
    const val APP_MODE = "URL" // ← Change to "LOCAL" to load from assets/www/ folder

    // ═════════════════════════════════════════════════════════════════════════
    //  MODE 1: WEBSITE URL (used when APP_MODE = "URL")
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * WEBSITE_URL — The full URL of the website to load inside the WebView.
     *
     * Requirements:
     *   - Must start with "https://" or "http://"
     *   - Must be a valid, publicly accessible URL
     *   - HTTP sites work because usesCleartextTraffic is enabled in the manifest
     *
     * Example:
     *   const val WEBSITE_URL = "https://www.google.com"
     *   const val WEBSITE_URL = "https://your-website.com"
     *   const val WEBSITE_URL = "http://192.168.1.100:3000"  // Local dev server
     */
    const val WEBSITE_URL = "https://electroxbd.com" // ← Replace with your website URL

    // ═════════════════════════════════════════════════════════════════════════
    //  MODE 2: LOCAL HTML (used when APP_MODE = "LOCAL")
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * LOCAL_START_FILE — The entry point HTML file for local mode.
     *
     * This file must exist inside: app/src/main/assets/www/
     *
     * The full path loaded will be: "file:///android_asset/www/{LOCAL_START_FILE}"
     *
     * Folder structure for your local project:
     *   assets/
     *     www/
     *       index.html       ← Your main HTML file (this constant points here)
     *       css/
     *         style.css      ← Your stylesheets
     *       js/
     *         script.js      ← Your JavaScript files
     *       img/
     *         logo.png       ← Your images
     *
     * Example:
     *   const val LOCAL_START_FILE = "index.html"
     *   const val LOCAL_START_FILE = "home.html"
     */
    const val LOCAL_START_FILE = "index.html" // ← Change if your entry file has a different name

    // ═════════════════════════════════════════════════════════════════════════
    //  ADMOB CONFIGURATION
    // ═════════════════════════════════════════════════════════════════════════
    //
    //  HOW ADS WORK IN THIS TEMPLATE:
    //    - If ALL three AdMob fields below are left empty (""), ads are
    //      completely disabled. No SDK initialization, no crashes, no issues.
    //    - If you provide valid IDs, ads activate automatically.
    //    - You can enable banner only, interstitial only, or both.
    //
    //  WHERE TO FIND YOUR IDs:
    //    1. Go to https://admob.google.com
    //    2. App ID:           Apps → Your App → App settings → App ID
    //    3. Banner Unit ID:   Apps → Your App → Ad units → Banner → Ad unit ID
    //    4. Interstitial ID:  Apps → Your App → Ad units → Interstitial → Ad unit ID
    //
    //  FOR TESTING (use Google's official test IDs — they show test ads):
    //    App ID:          "ca-app-pub-3940256099942544~3347511713"
    //    Banner ID:       "ca-app-pub-3940256099942544/6300978111"
    //    Interstitial ID: "ca-app-pub-3940256099942544/1033173712"
    //
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * ADMOB_APP_ID — Your AdMob Application ID.
     *
     * ⚠ IMPORTANT: This must also match the value in AndroidManifest.xml
     *   under the <meta-data> tag with name "com.google.android.gms.ads.APPLICATION_ID"
     *
     * Leave empty ("") to completely disable all ads.
     * Format: "ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"
     */
    const val ADMOB_APP_ID = "" // ← Paste your AdMob Application ID here (or leave "" to disable all ads)

    /**
     * ADMOB_BANNER_ID — Your AdMob Banner Ad Unit ID.
     *
     * Leave empty ("") to disable the banner ad (the bottom bar will be hidden).
     * Format: "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
     *
     * Test ID: "ca-app-pub-3940256099942544/6300978111"
     */
    const val ADMOB_BANNER_ID = "" // ← Paste your Banner Ad Unit ID here (or leave "" to disable banner)

    /**
     * ADMOB_INTERSTITIAL_ID — Your AdMob Interstitial Ad Unit ID.
     *
     * Leave empty ("") to disable interstitial ads entirely.
     * Format: "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
     *
     * Test ID: "ca-app-pub-3940256099942544/1033173712"
     */
    const val ADMOB_INTERSTITIAL_ID = "" // ← Paste your Interstitial Ad Unit ID here (or leave "" to disable interstitial)

    /**
     * INTERSTITIAL_INTERVAL_SECONDS — Minimum time (in seconds) between
     * two consecutive interstitial ad displays.
     *
     * This prevents spamming the user with too many full-screen ads.
     * The interstitial will only be shown when the user presses the back
     * button AND at least this many seconds have elapsed since the last one.
     *
     * Recommended: 60–180 seconds.
     * Set to 0 to show an interstitial on every eligible back press.
     *
     * Example:
     *   const val INTERSTITIAL_INTERVAL_SECONDS = 60   // Once per minute
     *   const val INTERSTITIAL_INTERVAL_SECONDS = 120  // Once every 2 minutes
     *   const val INTERSTITIAL_INTERVAL_SECONDS = 0    // Every back press
     */
    const val INTERSTITIAL_INTERVAL_SECONDS = 60 // ← Seconds between interstitial ads

    // ═════════════════════════════════════════════════════════════════════════
    //  UI CUSTOMIZATION
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * PROGRESS_BAR_COLOR — The color of the Chrome-style loading bar at the top.
     *
     * Must be a valid hex color string (e.g., "#4285F4").
     * Default is Google Blue (#4285F4).
     *
     * Example:
     *   const val PROGRESS_BAR_COLOR = "#FF5722"  // Deep Orange
     *   const val PROGRESS_BAR_COLOR = "#4CAF50"  // Green
     *   const val PROGRESS_BAR_COLOR = "#2196F3"  // Blue
     */
    const val PROGRESS_BAR_COLOR = "#4285F4" // ← Change to your preferred loading bar color

    /**
     * SWIPE_REFRESH_COLORS — The colors used for the pull-to-refresh spinner.
     *
     * These colors cycle in order as the spinner animates.
     * Provide hex color strings. The default uses Google's brand colors.
     *
     * Example:
     *   val SWIPE_REFRESH_COLORS = arrayOf("#FF5722")                    // Single color
     *   val SWIPE_REFRESH_COLORS = arrayOf("#4285F4", "#EA4335", "#FBBC05", "#34A853")  // Google colors
     */
    val SWIPE_REFRESH_COLORS = arrayOf("#4285F4", "#EA4335", "#FBBC05", "#34A853") // ← Pull-to-refresh spinner colors

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPER METHODS (do not modify)
    // ═════════════════════════════════════════════════════════════════════════

    /** Returns true if the app is configured to load a live website URL. */
    fun isUrlMode(): Boolean = APP_MODE.equals("URL", ignoreCase = true)

    /** Returns true if the app is configured to load local HTML from assets. */
    fun isLocalMode(): Boolean = APP_MODE.equals("LOCAL", ignoreCase = true)

    /** Returns true if the AdMob App ID is provided (non-blank). */
    fun isAdmobEnabled(): Boolean = ADMOB_APP_ID.isNotBlank()

    /** Returns true if the Banner Ad Unit ID is provided (non-blank). */
    fun isBannerEnabled(): Boolean = ADMOB_BANNER_ID.isNotBlank() && isAdmobEnabled()

    /** Returns true if the Interstitial Ad Unit ID is provided (non-blank). */
    fun isInterstitialEnabled(): Boolean = ADMOB_INTERSTITIAL_ID.isNotBlank() && isAdmobEnabled()

    /** Returns the full file:///android_asset/ URL for local mode. */
    fun getLocalUrl(): String = "file:///android_asset/www/$LOCAL_START_FILE"
}
