package com.fm.beebo.ui

import android.graphics.Bitmap
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient

class CustomWebViewClient(private val cookies: Map<String, String>) : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        val cookieManager = CookieManager.getInstance()
        cookies.forEach { (name, value) ->
            cookieManager.setCookie(url, "$name=$value")
        }
        cookieManager.flush()
        Log.d("CustomWebViewClient", "Cookies set for URL: $url")
    }
    fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
    }
}