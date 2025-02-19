package com.example.swiftycompanion.api

import com.example.swiftycompanion.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("users/{userId}")
    fun getCursusUserById(@Path("userId") cursusUserId: String): Call<User>
}

