package com.fm.beebo.network

import android.webkit.CookieManager
import com.fm.beebo.network.NetworkConfig.BASE_LOGGED_IN_URL
import com.fm.beebo.network.NetworkConfig.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup

class LoginService {

    /**
     * Get CSID to allow login if never logged in before then login using credentials
     * and return cookies. This version properly syncs cookies between HTTP and WebView.
     */
    suspend fun login(username: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cookieManager = CookieManager.getInstance()

                // Clear existing cookies to start fresh
                cookieManager.removeAllCookies(null)
                cookieManager.flush()

                // Step 1: Get initial response and extract cookies
                val initialResponse = Jsoup.connect(BASE_URL)
                    .timeout(30000)
                    .execute()

                val initialCookies = initialResponse.cookies()

                // Immediately sync initial cookies to WebView
                syncCookiesToWebView(initialCookies, BASE_LOGGED_IN_URL)

                val initialDoc = initialResponse.parse()
                val csidInput = initialDoc.select("input[name=CSId]").first()
                    ?: return@withContext false

                val csid = csidInput.attr("value")
                println("CSID: $csid")

                // Step 2: Send login request
                val loginResponse = Jsoup.connect("$BASE_LOGGED_IN_URL/webOPACClient/login.do")
                    .data("methodToCall", "submit")
                    .data("methodToCallParameter", "submitLogin")
                    .data("username", username)
                    .data("password", password)
                    .data("CSId", csid)
                    .cookies(initialCookies)  // Use the cookies from initial request
                    .timeout(30000)
                    .method(Connection.Method.POST)
                    .followRedirects(true)
                    .execute()

                val loginCookies = loginResponse.cookies()

                // Step 3: Sync login cookies to WebView immediately
                syncCookiesToWebView(loginCookies, BASE_LOGGED_IN_URL)

                // Force flush cookies to ensure they're persisted
                cookieManager.flush()

                // Step 4: Verify if login was successful
                val loginDoc = loginResponse.parse()

                // Debug: Print response details
                println("Login response URL: ${loginResponse.url()}")
                println("Login response status: ${loginResponse.statusCode()}")
                println("Page title: ${loginDoc.title()}")

                // Print all cookies for debugging
                println("Login cookies received:")
                loginCookies.forEach { (name, value) ->
                    println("  $name = $value")
                }

                // Check multiple indicators of successful login
                val loginError = loginDoc.select(".loginError, .error, div:contains(Fehler), div:contains(Error)")
                val isLoggedIn = loginDoc.select("a:contains(Abmelden), a:contains(Logout), a:contains(logout)")
                val loginSuccess = loginDoc.select(".loginSuccess, .success")
                val userInfo = loginDoc.select(".userInfo, .account, a[href*=account]")

                println("Login error elements: ${loginError.size}")
                println("Logout elements: ${isLoggedIn.size}")
                println("Success elements: ${loginSuccess.size}")
                println("User info elements: ${userInfo.size}")

                return@withContext when {
                    loginError.isNotEmpty() -> {
                        println("Login failed: ${loginError.text()}")
                        false
                    }
                    isLoggedIn.isNotEmpty() || loginSuccess.isNotEmpty() || userInfo.isNotEmpty() -> {
                        println("Login successful - found success indicators")
                        true
                    }
                    else -> {
                        // Check if we have session cookies that indicate login
                        val hasSessionCookie = loginCookies.any { (name, _) ->
                            name.lowercase().contains("session") ||
                                    name.lowercase().contains("jsession") ||
                                    name.lowercase().contains("auth")
                        }

                        val currentUrl = loginResponse.url().toString()
                        val pageContent = loginDoc.text().lowercase()

                        if (hasSessionCookie ||
                            currentUrl.contains("account") ||
                            currentUrl.contains("user") ||
                            pageContent.contains("willkommen") ||
                            pageContent.contains("angemeldet") ||
                            !currentUrl.contains("login")) {
                            println("Login appears successful based on cookies/URL/content")
                            true
                        } else {
                            println("Login status unclear. URL: $currentUrl")
                            println("Page content preview: ${pageContent.take(200)}...")
                            false
                        }

                    }
                }
            } catch (e: Exception) {
                println("Login exception: ${e.message}")
                e.printStackTrace()
                return@withContext false
            }
        }
    }

    /**
     * Properly sync cookies from HTTP response to WebView CookieManager
     */
    private fun syncCookiesToWebView(cookies: Map<String, String>, domain: String) {
        val cookieManager = CookieManager.getInstance()

        cookies.forEach { (name, value) ->
            val cookieString = "$name=$value; Domain=${domain.removePrefix("https://").removePrefix("http://").split("/")[0]}; Path=/"
            cookieManager.setCookie(domain, cookieString)
            println("Set cookie: $cookieString")
        }

        // Flush to ensure cookies are persisted
        cookieManager.flush()
    }

    /**
     * Get current cookies from WebView (for debugging)
     */
    fun getCurrentWebViewCookies(url: String): String? {
        return CookieManager.getInstance().getCookie(url)
    }
}