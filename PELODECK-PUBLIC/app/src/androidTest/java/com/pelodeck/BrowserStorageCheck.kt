package com.pelodeck

import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class BrowserStorageCheck {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val origin = "https://pelodeck-storage-check.invalid/"

    private fun open(): WebView {
        lateinit var web: WebView
        val ready = CountDownLatch(1)
        instrumentation.runOnMainSync {
            web = WebView(instrumentation.targetContext)
            web.settings.javaScriptEnabled = true
            web.settings.domStorageEnabled = true
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)
            web.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) { ready.countDown() }
            }
            web.loadDataWithBaseURL(origin, "<html><body>Storage check</body></html>", "text/html", null, origin)
        }
        assertTrue("WebView did not load", ready.await(15, TimeUnit.SECONDS))
        return web
    }

    private fun js(web: WebView, script: String): String {
        var result = ""
        val done = CountDownLatch(1)
        instrumentation.runOnMainSync { web.evaluateJavascript(script) { result = it; done.countDown() } }
        assertTrue("JavaScript did not finish", done.await(10, TimeUnit.SECONDS))
        return result
    }

    @Test fun writeFixture() {
        val web = open()
        println("Origin: " + js(web, "location.origin"))
        assertEquals("\"ok\"", js(web, "localStorage.setItem('pelodeck_check','saved'); 'ok'"))
        assertEquals("\"saved\"", js(web, "localStorage.getItem('pelodeck_check')"))
        val done = CountDownLatch(1)
        instrumentation.runOnMainSync { CookieManager.getInstance().setCookie(origin, "pelodeck_check=saved; Max-Age=600; Path=/; Secure") { done.countDown() } }
        assertTrue("Cookie write did not finish", done.await(10, TimeUnit.SECONDS))
        CookieManager.getInstance().flush()
        Thread.sleep(6000)
        instrumentation.runOnMainSync { web.destroy() }
    }

    @Test fun readAndRemoveFixture() {
        val web = open()
        try {
            val stored = js(web, "localStorage.getItem('pelodeck_check')")
            val cookie = CookieManager.getInstance().getCookie(origin)
            println("Origin: " + js(web, "location.origin") + "; storage=" + stored + "; cookiePresent=" + (cookie?.contains("pelodeck_check=saved") == true))
            assertEquals("\"saved\"", stored)
            assertTrue("Persistent cookie missing", cookie?.contains("pelodeck_check=saved") == true)
        } finally {
            js(web, "localStorage.removeItem('pelodeck_check')")
            val done = CountDownLatch(1)
            instrumentation.runOnMainSync { CookieManager.getInstance().setCookie(origin, "pelodeck_check=; Max-Age=0; Path=/; Secure") { done.countDown() } }
            done.await(10, TimeUnit.SECONDS)
            CookieManager.getInstance().flush()
            instrumentation.runOnMainSync { web.destroy() }
        }
    }
}
