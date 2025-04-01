package com.fm.beebo.ui

import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fm.beebo.network.printCookies

class CustomWebViewClient(private val cookies: Map<String, String>) : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        val cookieManager = CookieManager.getInstance()
        cookies.forEach { (name, value) ->
            cookieManager.setCookie(url, "$name=$value")
        }
        CookieManager.getInstance().printCookies()
        val webSettings = view?.settings
        webSettings?.javaScriptEnabled = true
    }
}