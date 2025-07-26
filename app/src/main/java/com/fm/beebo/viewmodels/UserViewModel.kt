package com.fm.beebo.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fm.beebo.network.LoginService
import kotlinx.coroutines.launch
import kotlin.math.log

class UserViewModel : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var accountFees by mutableStateOf<String?>(null)

    private val loginService = LoginService()

    fun login() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val loginSuccess = loginService.login(username, password)
                if (loginSuccess) {
                    isLoggedIn = true
                    fetchAccountDetails()
                } else {
                    errorMessage = "Login fehlgeschlagen"
                }
            } catch (e: Exception) {
                errorMessage = "Fehler beim Login: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchAccountDetails() {
        viewModelScope.launch {
            try {
                val fees = loginService.fetchAccountFees()
                accountFees = fees
            } catch (e: Exception) {
                errorMessage = "Fehler beim Abrufen der Kontodaten: ${e.message}"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            loginService.logout()
            isLoggedIn = false
            accountFees = null
        }
    }
}
