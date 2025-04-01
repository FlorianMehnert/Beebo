package com.fm.beebo.network

import android.webkit.CookieManager
import com.fm.beebo.network.NetworkConfig.BASE_LOGGED_IN_URL
import com.fm.beebo.network.NetworkConfig.BASE_URL

fun CookieManager.configure() {
    this.setAcceptCookie(true)
    this.flush()
}


fun CookieManager.setCookies(baseUrl: String, cookies: Map<String, String>) {
    cookies.forEach { (key, value) ->
        this.setCookie(baseUrl, "$key=$value")
    }
}

fun CookieManager.printCookies(
){
    val cookiesString = this.getCookie(BASE_LOGGED_IN_URL) ?: ""
    print(cookiesString)
}

fun CookieManager.getCookies(): Map<String, String> {
    val cookiesString = this.getCookie(BASE_URL) ?: ""
    return cookiesString.split("; ")
        .mapNotNull { it.split("=").takeIf { it.size == 2 } }
        .associate { it[0] to it[1] }
}
