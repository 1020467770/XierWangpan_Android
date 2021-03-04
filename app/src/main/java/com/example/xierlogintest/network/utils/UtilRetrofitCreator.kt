package com.example.xierlogintest.network.utils

import com.example.xierlogintest.activity.ActivityController
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UtilRetrofitCreator {
    //39.97.183.4
    private const val BASE_URL = "http://10.0.2.2:8080/TESTS/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(genericClient())
        .build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    private fun genericClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .addHeader("Cookie","JSESSIONID=${ActivityController.COOKIE}")
                .build()
            chain.proceed(request);
        }).build()
        return httpClient

    }

}