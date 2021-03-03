package com.example.xierlogintest.network.Interceptor

import android.text.TextUtils
import com.example.xierlogintest.activity.ActivityController
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AddCookiesInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
        if(!TextUtils.isEmpty(ActivityController.COOKIE)){
            builder.addHeader("Cookie", ActivityController.COOKIE);
        }
        return chain.proceed(builder.build());
    }
}