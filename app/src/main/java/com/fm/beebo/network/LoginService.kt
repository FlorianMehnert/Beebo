package com.fm.beebo.network

import android.webkit.CookieManager
import com.fm.beebo.network.NetworkConfig.BASE_LOGGED_IN_URL
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

                // Clear old session and CSId
                cookieManager.removeAllCookies(null)
                cookieManager.clearCSId()
                cookieManager.flush()
                println("🔄 Cleared old session and CSId")


                // Step 1: Initialize session with proper URL
                val initialResponse =
                    Jsoup.connect(NetworkConfig.BASE_URL) // ✅ Use BASE_URL for initialization
                        .timeout(30000)
                        .execute()


                val sessionCookies = initialResponse.cookies()
                cookieManager.syncFromHttpClient(sessionCookies, BASE_LOGGED_IN_URL)

                // Step 2: Extract and store CSId
                val initialDoc = initialResponse.parse()
                val csidInput = initialDoc.select("input[name=CSId]").first()
                    ?: return@withContext false
                val csid = csidInput.attr("value")

                // ✅ Store CSId for later use
                cookieManager.storeCSId(csid)
                println("🔑 Stored CSId: $csid")


                // Step 3: Login with session cookies
                val loginResponse = Jsoup.connect("$BASE_LOGGED_IN_URL/webOPACClient/login.do")
                    .data("methodToCall", "submit")
                    .data("methodToCallParameter", "submitLogin")
                    .data("username", username)
                    .data("password", password)
                    .data("CSId", csid) // Use extracted CSId
                    .cookies(sessionCookies)
                    .timeout(30000)
                    .method(Connection.Method.POST)
                    .followRedirects(true)
                    .execute()


                // Update cookies after login
                val loginCookies = loginResponse.cookies()
                val finalCookies = sessionCookies.toMutableMap().apply {
                    putAll(loginCookies)
                }

                cookieManager.syncFromHttpClient(finalCookies, BASE_LOGGED_IN_URL)
                cookieManager.debugSessionState()

                // Step 4: Verify login
                val loginDoc = loginResponse.parse()
                val isLoggedIn =
                    loginDoc.select("a:contains(Abmelden), a:contains(Logout)").isNotEmpty()

                if (isLoggedIn) {
                    println("✅ Login successful - session and CSId established")
                } else {
                    println("❌ Login failed")
                    cookieManager.clearCSId() // Clear CSId on failed login
                }

                return@withContext isLoggedIn


            } catch (e: Exception) {
                println("🚨 Login exception: ${e.message}")
                CookieManager.getInstance().clearCSId()
                return@withContext false
            }
        }
    }

    /**
     * Properly sync cookies from HTTP response to WebView CookieManager
     */
    private fun syncCookiesToWebView(cookies: Map<String, String>, domain: String) {
        val cookieManager = CookieManager.getInstance()

        // Extract just the domain (without protocol, path, etc.)
        val cleanDomain = domain.removePrefix("https://").removePrefix("http://").split("/")[0]

        cookies.forEach { (name, value) ->
            // Set cookie with proper format - let WebView handle domain/path automatically
            val cookieString = "$name=$value; Domain=${
                domain.removePrefix("https://").removePrefix("http://").split("/")[0]
            }; Path=/"
            cookieManager.setCookie(domain, cookieString)

            println("Set cookie for WebView: $name=$value on domain: $cleanDomain")
        }

        // Force immediate flush
        cookieManager.flush()

        // Wait a bit for flush to complete
        Thread.sleep(100)
    }
}