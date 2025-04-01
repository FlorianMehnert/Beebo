package com.fm.beebo.network

import com.fm.beebo.network.NetworkConfig.BASE_LOGGED_IN_URL
import com.fm.beebo.network.NetworkConfig.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup

class LoginService{

    /**
     * Get CSID to allow login if never logged in before then login using credentials
     * and return cookies
     */
    suspend fun login(username: String, password: String): Map<String, String>? {
        return withContext(Dispatchers.IO) {
            val cookies = mutableMapOf<String, String>()
            cookies.putAll(initialResponse.cookies())

            try {
                val initialResponse = Jsoup.connect(BASE_URL)
                    .timeout(30000)
                    .execute()

                // Store initial cookies
                cookies.putAll(initialResponse.cookies())

                val initialDoc = initialResponse.parse()
                val csidInput = initialDoc.select("input[name=CSId]").first()
                    ?: return@withContext null

                val csid = csidInput.attr("value")

                // Step 2: Send login request
                val loginResponse = Jsoup.connect("$BASE_LOGGED_IN_URL/webOPACClient/login.do")
                    .data("username", username)
                    .data("password", password)
                    .data("CSId", csid)
                    .cookies(cookies)
                    .timeout(30000)
                    .method(Connection.Method.POST)
                    .execute()

                // Step 3: Update cookies after login
                cookies.putAll(loginResponse.cookies())

                // Step 4: Verify if login was successful
                val loginDoc = loginResponse.parse()
                val loginError = loginDoc.select(".loginError") // Adjust the selector if necessary

                return@withContext if (loginError.isNotEmpty()) {
                    println("Login failed: ${loginError.text()}")
                    null
                } else {
                    println("Login successful")
                    cookies
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }
}
