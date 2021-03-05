package com.example.xierlogintest

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class XierWangpanApplication : Application() {

    companion object {

        const val BASE_URI = "39.97.183.4:8080"

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}