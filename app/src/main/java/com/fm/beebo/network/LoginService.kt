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

                // Step 2: Send login request
                var cookies = cookieManager.getCookies()
                val loginResponse = Jsoup.connect("$BASE_LOGGED_IN_URL/webOPACClient/login.do")
                    .data("username", username)
                    .data("password", password)
                    .data("CSId", csid)
                    .cookies(cookies)
                    .timeout(30000)
                    .method(Connection.Method.POST)
                    .execute()
                val newCookies = loginResponse.cookies()

                // Step 3: Update cookies after login
                cookieManager.setCookies(BASE_LOGGED_IN_URL, newCookies)

                // Step 4: Verify if login was successful
                val loginDoc = loginResponse.parse()
                val loginError = loginDoc.select(".loginError") // Adjust the selector if necessary

                return@withContext if (loginError.isNotEmpty()) {
                    println("Login failed: ${loginError.text()}")
                    false
                } else {
                    println("Login successful")
                    println(loginResponse.body())
                    true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }
        }
    }
}
