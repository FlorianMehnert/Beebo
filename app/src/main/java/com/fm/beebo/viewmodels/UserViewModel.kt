package com.fm.beebo.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fm.beebo.network.LoginService
import kotlinx.coroutines.launch


class UserViewModel : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val loginService = LoginService()

    fun login() {
        viewModelScope.launch {
            loginService.login(username, password)
            isLoggedIn = true
        }
    }

    fun logout(){
        viewModelScope.launch {
            loginService.logout()
            isLoggedIn = false
        }
    }
}