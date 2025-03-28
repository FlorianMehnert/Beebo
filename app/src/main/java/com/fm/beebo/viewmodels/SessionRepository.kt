package com.fm.beebo.viewmodels

class SessionRepository private constructor() {
    private val _cookies = mutableMapOf<String, String>()

    val cookies: Map<String, String>
        get() = _cookies

    fun updateCookies(newCookies: Map<String, String>) {
        _cookies.putAll(newCookies)
    }

    companion object {
        @Volatile
        private var INSTANCE: SessionRepository? = null

        fun getInstance(): SessionRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionRepository().also { INSTANCE = it }
            }
    }
}