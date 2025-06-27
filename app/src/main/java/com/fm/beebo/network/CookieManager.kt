package com.fm.beebo.network

import android.webkit.CookieManager

fun CookieManager.configure() {
    this.setAcceptCookie(true)
    this.flush()
}


fun CookieManager.setCookies(baseUrl: String, cookies: Map<String, String>) {
    cookies.forEach { (key, value) ->
        this.setCookie(baseUrl, "$key=$value")
    }
}

fun CookieManager.getCookies(host: String = NetworkConfig.BASE_LOGGED_IN_URL)
        : Map<String, String> =
    (getCookie(host) ?: "")
        .split("; ")
        .mapNotNull { it.split("=").takeIf { it.size == 2 } }
        .associate { it[0] to it[1] }

fun CookieManager.syncToHttpClient(): Map<String, String> {
    val cookiesString = this.getCookie(NetworkConfig.BASE_LOGGED_IN_URL) ?: ""
    return cookiesString.split("; ")
        .mapNotNull {
            val parts = it.split("=", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }
        .toMap()
}

fun CookieManager.syncFromHttpClient(cookies: Map<String, String>, domain: String) {
    cookies.forEach { (name, value) ->
        // Simple format - let WebView handle domain/path automatically
        this.setCookie(domain, "$name=$value")
    }
    this.flush()
}

fun CookieManager.debugPrintCookies(context: String = "") {
    val cookies = this.getCookie(NetworkConfig.BASE_LOGGED_IN_URL) ?: "No cookies"
    println("🍪 [$context] WebView Cookies: $cookies")
}

// CookieManager.kt - Add session validation
fun CookieManager.isSessionValid(): Boolean {
    val cookies = this.getCookie(NetworkConfig.BASE_LOGGED_IN_URL) ?: return false
    return cookies.contains("JSESSIONID") && cookies.contains("USERSESSIONID")
}

fun CookieManager.debugSessionState() {
    val cookies = this.getCookie(NetworkConfig.BASE_LOGGED_IN_URL) ?: "No cookies"
    println("🔍 Session Debug: $cookies")
}

fun CookieManager.storeCSId(csid: String) {
    // Store CSId as a special cookie
    this.setCookie(NetworkConfig.BASE_LOGGED_IN_URL, "APP_CSID=$csid")
    this.flush()
}


fun CookieManager.getStoredCSId(): String {
    val cookies = this.getCookie(NetworkConfig.BASE_LOGGED_IN_URL) ?: return ""
    return cookies.split("; ")
        .find { it.startsWith("APP_CSID=") }
        ?.substringAfter("APP_CSID=") ?: ""
}


fun CookieManager.clearCSId() {
    // Remove the CSId cookie
    this.setCookie(NetworkConfig.BASE_LOGGED_IN_URL, "APP_CSID=; Max-Age=0")
    this.flush()
}