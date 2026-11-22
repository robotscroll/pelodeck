package com.pelodeck.ui.dashboard.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.pelodeck.data.local.entity.Bookmark
import com.pelodeck.diagnostics.DiagnosticLogger
import com.pelodeck.ui.theme.PeloDeckTheme

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CenterBrowserPanel(
    bookmarks: List<Bookmark>,
    classRequest: Pair<Int, String>? = null,
    onAddBookmark: (String, String) -> Unit,
    onFullscreenChanged: (View?, (() -> Unit)?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = PeloDeckTheme.colors

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf("https://www.youtube.com") }
    var inputUrl by remember { mutableStateOf("https://www.youtube.com") }
    var pageTitle by remember { mutableStateOf("YouTube") }
    var isLoading by remember { mutableStateOf(false) }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(classRequest, webViewInstance) {
        val request = classRequest
        val browser = webViewInstance
        if (request != null && browser != null) {
            val url = "https://www.youtube.com/results?search_query=" + Uri.encode(request.second)
            inputUrl = url
            browser.loadUrl(url)
        }
    }

    // HTML5 Fullscreen Video Handling
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    fun exitFullscreen() {
        val callback = customViewCallback
        customView = null
        customViewCallback = null
        onFullscreenChanged(null, null)
        callback?.onCustomViewHidden()
    }

    BackHandler(enabled = customView != null || (webViewInstance?.canGoBack() == true)) {
        if (customView != null) {
            exitFullscreen()
        } else if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        }
    }

    // Flush cookies periodically or on dispose
    DisposableEffect(Unit) {
        onDispose {
            CookieManager.getInstance().flush()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(1.dp, colors.panelBorder, RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Address bar & Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.barBackground)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { webViewInstance?.goBack() },
                    enabled = webViewInstance?.canGoBack() == true,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (webViewInstance?.canGoBack() == true) colors.primary else colors.textSecondary.copy(alpha = 0.4f)
                    )
                }

                IconButton(
                    onClick = { webViewInstance?.goForward() },
                    enabled = webViewInstance?.canGoForward() == true,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (webViewInstance?.canGoForward() == true) colors.primary else colors.textSecondary.copy(alpha = 0.4f)
                    )
                }

                IconButton(
                    onClick = { webViewInstance?.reload() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = colors.primary
                    )
                }

                IconButton(
                    onClick = {
                        val home = "https://www.youtube.com"
                        inputUrl = home
                        webViewInstance?.loadUrl(home)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = colors.secondary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // URL Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.surfaceVariant)
                        .border(1.dp, colors.panelBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = colors.textPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        ),
                        cursorBrush = SolidColor(colors.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                val urlToLoad = normalizeUrl(inputUrl)
                                if (urlToLoad != null) { inputUrl = urlToLoad; webViewInstance?.loadUrl(urlToLoad) } else { errorMessage = "Only HTTPS addresses are supported." }
                            }
                        )
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = {
                        val urlToLoad = normalizeUrl(inputUrl)
                        if (urlToLoad != null) { inputUrl = urlToLoad; webViewInstance?.loadUrl(urlToLoad) } else { errorMessage = "Only HTTPS addresses are supported." }
                    },
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.buttonBackground,
                        contentColor = colors.buttonText
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "GO",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }

            // Bookmarks Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceVariant)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                bookmarks.forEach { bookmark ->
                    FilterChip(
                        selected = currentUrl.contains(bookmark.url.replace("https://", "").replace("www.", "")),
                        onClick = {
                            val url = secureUrl(bookmark.url)
                            if (url != null) { inputUrl = url; webViewInstance?.loadUrl(url) } else { errorMessage = "Only HTTPS bookmarks are supported." }
                        },
                        label = {
                            Text(
                                text = bookmark.title,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colors.surface,
                            labelColor = colors.textPrimary,
                            selectedContainerColor = colors.buttonBackground,
                            selectedLabelColor = colors.primary
                        )
                    )
                }

                // Add Current Page Bookmark
                FilterChip(
                    selected = false,
                    onClick = {
                        val title = pageTitle.ifBlank { "Page" }
                        onAddBookmark(title, currentUrl)
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "Add", fontSize = 11.sp)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colors.surface,
                        labelColor = colors.secondary
                    )
                )
            }

            // Progress bar
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { loadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = colors.primary,
                    trackColor = colors.surfaceVariant
                )
            }

            // Main WebView or Error View
            Box(modifier = Modifier.fillMaxSize()) {
                if (errorMessage != null) {
                    // Friendly Retro Error Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.surface)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "[CONNECTION FAILURE]",
                            color = colors.secondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Failed to load page.",
                            color = colors.textSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                errorMessage = null
                                webViewInstance?.reload()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.buttonBackground,
                                contentColor = colors.buttonText
                            )
                        ) {
                            Text("RETRY CONNECTION")
                        }
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            // Setup Cookies
                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            // WebSettings
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                mediaPlaybackRequiresUserGesture = false
                                builtInZoomControls = true
                                displayZoomControls = false
                                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                                cacheMode = WebSettings.LOAD_DEFAULT
                                userAgentString = "${settings.userAgentString} PeloDeck/1.0"
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    loadProgress = newProgress / 100f
                                    isLoading = newProgress < 100
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    if (!title.isNullOrBlank()) {
                                        pageTitle = title
                                    }
                                }

                                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                    if (view == null) { callback?.onCustomViewHidden(); return }
                                    if (customView != null) { callback?.onCustomViewHidden(); return }
                                    customView = view
                                    customViewCallback = callback
                                    onFullscreenChanged(view, ::exitFullscreen)
                                    DiagnosticLogger.i("Browser", "Entered HTML5 Fullscreen Video View")
                                }

                                override fun onHideCustomView() {
                                    customView = null
                                    customViewCallback = null
                                    onFullscreenChanged(null, null)
                                    DiagnosticLogger.i("Browser", "Exited HTML5 Fullscreen Video View")
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                    errorMessage = null
                                    url?.let {
                                        currentUrl = it
                                        inputUrl = it
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                    CookieManager.getInstance().flush()
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    if (request?.isForMainFrame == true) {
                                        isLoading = false
                                        errorMessage = error?.description?.toString() ?: "Network request failed"
                                        DiagnosticLogger.w("Browser", "A page failed to load")
                                    }
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val uri = request?.url ?: return false
                                    if (secureUrl(uri.toString()) != null) return false
                                    if (request.isForMainFrame) errorMessage = "Only HTTPS addresses are supported."
                                    return true
                                }
                            }

                            loadUrl(currentUrl)
                            webViewInstance = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )


            }
        }
    }
}

private fun secureUrl(value: String): String? {
    val uri = runCatching { Uri.parse(value.trim()) }.getOrNull() ?: return null
    if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank() || uri.userInfo != null) return null
    return uri.toString()
}

private fun normalizeUrl(value: String): String? {
    val text = value.trim()
    if (text.isBlank()) return null
    if (text.contains(Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:"))) return secureUrl(text)
    return if (text.contains('.') && !text.contains(' ')) secureUrl("https://$text")
        else "https://www.google.com/search?q=${Uri.encode(text)}"
}
