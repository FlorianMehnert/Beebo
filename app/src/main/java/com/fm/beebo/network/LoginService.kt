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
     * and return cookies
     */
    suspend fun login(username: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cookieManager = CookieManager.getInstance()

                // Step 1: Get initial response and store cookies
                val initialResponse = Jsoup.connect(BASE_URL)
                    .timeout(30000)
                    .execute()

                cookieManager.setCookies(BASE_LOGGED_IN_URL, initialResponse.cookies())

                val initialDoc = initialResponse.parse()
                val csidInput = initialDoc.select("input[name=CSId]").first()
                    ?: return@withContext false

                val csid = csidInput.attr("value")

                // Step 2: Send login request with proper cookie handling
                val loginResponse = Jsoup.connect("$BASE_LOGGED_IN_URL/webOPACClient/login.do")
                    .data("methodToCall", "submit")  // Add this - likely required
                    .data("methodToCallParameter", "submitLogin")  // Add this - likely required
                    .data("username", username)
                    .data("password", password)
                    .data("CSId", csid)
                    .cookies(initialResponse.cookies())  // Use the original cookies directly
                    .timeout(30000)
                    .method(Connection.Method.POST)
                    .followRedirects(true)  // Important: follow redirects after login
                    .execute()

                // Step 3: Update cookies after login
                cookieManager.setCookies(BASE_LOGGED_IN_URL, loginResponse.cookies())

                // Step 4: Verify if login was successful
                val loginDoc = loginResponse.parse()

                // Debug: Print response details
                println("Login response URL: ${loginResponse.url()}")
                println("Login response status: ${loginResponse.statusCode()}")
                println("Page title: ${loginDoc.title()}")
                println("Page text contains 'angemeldet': ${loginDoc.text().lowercase().contains("angemeldet")}")

                // Check multiple indicators of successful login
                val loginError = loginDoc.select(".loginError, .error, div:contains(Fehler)")
                val isLoggedIn = loginDoc.select("a:contains(Abmelden), a:contains(Logout)")
                val loginSuccess = loginDoc.select(".loginSuccess, .success")

                // Additional check: look for user-specific content or account info
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
                        println("Login successful")
                        true
                    }
                    else -> {
                        // If we can't determine success, check the URL or page content
                        val currentUrl = loginResponse.url().toString()
                        val pageContent = loginDoc.text().lowercase()

                        if (currentUrl.contains("account") ||
                            currentUrl.contains("user") ||
                            pageContent.contains("willkommen") ||
                            pageContent.contains("angemeldet")) {
                            println("Login appears successful based on URL/content")
                            true
                        } else {
                            println("Login status unclear. URL: $currentUrl")
                            println("Page title: ${loginDoc.title()}")
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
}