package com.example.xierlogintest.activity

import android.app.Activity
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.example.xierlogintest.adapter.FileAdapter
import com.example.xierlogintest.model.BasicFile
import com.example.xierlogintest.model.User
import com.example.xierlogintest.network.service.RetrofitApiService
import com.example.xierlogintest.network.utils.UtilRetrofitCreator
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ActivityController {

    private val activities = ArrayList<Activity>()

    var isOnline: Boolean = true

    var loginUser: User? = null

    var currentFolderId: Int = 0

    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    fun removeActivity(activity: Activity) {
        activities.remove(activity)
    }

    fun finishAll() {
        for (activity in activities) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
        activities.clear()
    }
}