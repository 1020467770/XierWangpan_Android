package com.example.xierlogintest.network.Interceptor

import android.util.Log
import com.example.xierlogintest.activity.ActivityController
import okhttp3.Interceptor
import okhttp3.Response

class ReceivedCookiesInterceptor: Interceptor {
    private val TAG = "ReceivedCookiesIntercep"
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        if (!originalResponse.headers("Set-Cookie").isEmpty()) {

            //解析Cookie
            for (header in originalResponse.headers("Set-Cookie")) {
                if(header.contains("JSESSIONID")){
                    ActivityController.COOKIE = header.substring(header.indexOf("JSESSIONID"), header.indexOf(";"));
                    Log.d(TAG, "intercept: cookie=${ActivityController.COOKIE}")
                }
            }

        }

        return originalResponse;

    }

}