package com.veyra.app

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class PlayerActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    // 🔥 LIGHTWEIGHT AD-BLOCKER: List of known ad/tracking domains to block
    private val adDomains = setOf(
        "doubleclick.net",
        "googlesyndication.com",
        "adservice.google.com",
        "popads.net",
        "exoclick.com",
        "trafficjunky.com",
        "juicyads.com",
        "adnxs.com",
        "taboola.com",
        "outbrain.com",
        "advertising.com",
        "casalemedia.com",
        "rubiconproject.com",
        "openx.net",
        "pubmatic.com",
        "scorecardresearch.com",
        "flurry.com",
        "appsflyer.com",
        "adjust.com"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )

        setContentView(R.layout.activity_player)
        supportActionBar?.hide()

        val movieId = intent.getIntExtra("MOVIE_ID", 0)
        val isTvShow = intent.getBooleanExtra("IS_TV_SHOW", false)
        val movieTitle = intent.getStringExtra("MOVIE_TITLE") ?: "Unknown"

        webView = findViewById(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.allowFileAccess = true
        
        // BLOCK POPUP ADS natively
        webView.settings.setSupportMultipleWindows(false)
        webView.settings.javaScriptCanOpenWindowsAutomatically = false

        // 🔥 CUSTOM WEBVIEW CLIENT WITH AD-BLOCKING
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString()?.lowercase() ?: return super.shouldInterceptRequest(view, request)
                
                // Check if the request URL contains any known ad domain
                for (domain in adDomains) {
                    if (url.contains(domain)) {
                        // Block the ad by returning an empty 204 No Content response
                        return WebResourceResponse(null, null, null)
                    }
                }
                
                // If it's not an ad, let it load normally
                return super.shouldInterceptRequest(view, request)
            }
        }

        // Handle HTML5 Fullscreen Video properly
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                (window.decorView as ViewGroup).addView(customView)
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            override fun onHideCustomView() {
                if (customView == null) return
                (window.decorView as ViewGroup).removeView(customView)
                customView = null
                customViewCallback?.onCustomViewHidden()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
            }
        }

        val videoUrl = if (isTvShow) {
            val season = intent.getIntExtra("SEASON", 1)
            val episode = intent.getIntExtra("EPISODE", 1)
            "https://vidnest.fun/tv/$movieId/$season/$episode"
        } else {
            "https://vidnest.fun/movie/$movieId"
        }

        webView.loadUrl(videoUrl)

        // SAVE TO WATCH HISTORY
        WatchHistory.addToHistory(this, Movie(
            id = movieId,
            title = movieTitle,
            posterPath = null,
            isTvShow = isTvShow
        ))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    override fun onBackPressed() {
        if (customView != null) {
            webView.webChromeClient?.onHideCustomView()
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
        webView.clearHistory()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}