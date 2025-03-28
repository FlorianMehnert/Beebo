package com.fm.beebo.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fm.beebo.network.LoginService
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val loginService = LoginService()

    fun login() {
        viewModelScope.launch {
            // Update repository with new session cookies
            var newCookies = loginService.login(username, password)
            if (newCookies != null){
                SessionRepository.getInstance()
                    .updateCookies(newCookies)
                isLoggedIn = true
            }
        }
    }

}