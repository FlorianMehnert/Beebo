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

                if (!isLoggedIn) {
                    cookieManager.clearCSId() // Clear CSId on failed login
                }

                return@withContext isLoggedIn


            } catch (e: Exception) {
                CookieManager.getInstance().clearCSId()
                return@withContext false
            }
        }
    }

    suspend fun logout(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cookieManager = CookieManager.getInstance()
                cookieManager.removeAllCookies(null)
                cookieManager.clearCSId()
                cookieManager.flush()
                val initialResponse =
                    Jsoup.connect(NetworkConfig.BASE_URL)
                        .timeout(30000)
                        .execute()
                val sessionCookies = initialResponse.cookies()
                cookieManager.syncFromHttpClient(sessionCookies, BASE_LOGGED_IN_URL)
                val initialDoc = initialResponse.parse()
                val csidInput = initialDoc.select("input[name=CSId]").first()
                    ?: return@withContext false
                val csid = csidInput.attr("value")
                cookieManager.storeCSId(csid)
                return@withContext true
            } catch (_: Exception) {
                CookieManager.getInstance().clearCSId()
                return@withContext false
            }
        }
    }

}