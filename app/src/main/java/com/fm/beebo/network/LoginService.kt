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

    // Update your LoginService.kt fetchAccountFees method:
    suspend fun fetchAccountFees(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val cookieManager = CookieManager.getInstance()
                val cookies = cookieManager.getCookies()
                val csid = cookieManager.getStoredCSId()

                if (cookies.isEmpty() || csid.isEmpty()) {
                    return@withContext null
                }

                // First, we need to navigate to the account page
                // The login.html shows we're logged in but not on the account details page
                val accountUrl = "$BASE_LOGGED_IN_URL/webOPACClient/userAccount.do?methodToCall=showAccount&accountTyp=FEES"

                val response = Jsoup.connect(accountUrl)
                    .cookies(cookies)
                    .timeout(30000)
                    .execute()

                val doc = response.parse()

                // Check for session expiry
                if (doc.select("div.error").text().contains("Diese Sitzung ist nicht mehr gültig!")) {
                    return@withContext null
                }

                // If we're not on the fees page, try the direct link
                if (!doc.html().contains("showAccount(8)")) {
                    // Try alternative URL format
                    val feesUrl = "$BASE_LOGGED_IN_URL/webOPACClient/userAccount.do?methodToCall=showAccount&type=8"
                    val feesResponse = Jsoup.connect(feesUrl)
                        .cookies(cookies)
                        .timeout(30000)
                        .execute()

                    val feesDoc = feesResponse.parse()

                    // Parse fees from this page
                    val feesText = feesDoc.select("td:contains(EUR)").text()
                    if (feesText.isNotEmpty()) {
                        val feePattern = "(\\d+,\\d+)\\s*EUR".toRegex()
                        val match = feePattern.find(feesText)
                        return@withContext match?.groupValues?.get(1)?.let { "$it EUR" } ?: "0,00 EUR"
                    }
                }

                // Parse the fees from the page
                val feesElement = doc.select("a[onclick*='showAccount(8)']").firstOrNull()
                if (feesElement != null) {
                    val feesText = feesElement.text()
                    // Extract the fee amount from text like "Gebühren (0,20 EUR)"
                    val feePattern = "\\((\\d+,\\d+)\\s*EUR\\)".toRegex()
                    val match = feePattern.find(feesText)
                    return@withContext match?.groupValues?.get(1)?.let { "$it EUR" } ?: "0,00 EUR"
                }

                // If no fees link found, look for fees in the page content
                val feesText = doc.select("td:contains(Gebühren)").text()
                if (feesText.isNotEmpty()) {
                    val feePattern = "(\\d+,\\d+)\\s*EUR".toRegex()
                    val match = feePattern.find(feesText)
                    return@withContext match?.groupValues?.get(1)?.let { "$it EUR" } ?: "0,00 EUR"
                }

                return@withContext "0,00 EUR"
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
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