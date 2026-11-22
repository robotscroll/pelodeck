package com.pelodeck

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.webkit.WebViewAssetLoader

class WhiskersActivity : ComponentActivity() {
    private lateinit var game: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loader = WebViewAssetLoader.Builder().addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this)).build()
        game = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.mediaPlaybackRequiresUserGesture = true
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? = loader.shouldInterceptRequest(request.url)
            }
            loadUrl("https://appassets.androidplatform.net/assets/games/whiskers/index.html")
        }
        val root = FrameLayout(this)
        root.addView(game, FrameLayout.LayoutParams(-1, -1))
        root.addView(Button(this).apply { text = "Exit game"; setOnClickListener { finish() } },
            FrameLayout.LayoutParams(-2, -2, android.view.Gravity.BOTTOM or android.view.Gravity.END))
        setContentView(root)
    }
    override fun onDestroy() { game.destroy(); super.onDestroy() }
}
