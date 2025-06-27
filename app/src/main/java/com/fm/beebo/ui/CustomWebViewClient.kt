package com.fm.beebo.ui

import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient

class CustomWebViewClient(private val cookies: Map<String, String>) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        val cookieManager = CookieManager.getInstance()

        // Set initial cookies
        cookies.forEach { (name, value) ->
            cookieManager.setCookie(url, "$name=$value")
        }

        // Enable settings
        view?.settings?.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        // Sync any new cookies back to the cookie manager
        val cookieManager = CookieManager.getInstance()
        cookieManager.flush()
    }
}

