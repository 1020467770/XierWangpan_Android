package com.example.xierlogintest.network.utils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UtilRetrofitCreator {

    private const val BASE_URL = "http://10.0.2.2:8080/TESTS/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> create(serviceClass: Class<T>):T = retrofit.create(serviceClass)

}