package com.example.xierlogintest.dao

import android.content.Context
import androidx.core.content.edit
import com.example.xierlogintest.XierWangpanApplication
import com.example.xierlogintest.model.User
import com.google.gson.Gson

object UserDao {
    fun saveUser(user: User) {
        sharedPreferences().edit {
            putString("user", Gson().toJson(user))
        }
    }

    fun removeUser(){
        sharedPreferences().edit {
            remove("user")
        }
    }

    fun getSavedUser(): User {
        val userJson = sharedPreferences().getString("user", "")
        return Gson().fromJson(userJson, User::class.java)
    }

    fun isUserSaved() = sharedPreferences().contains("user")

    private fun sharedPreferences() =
        XierWangpanApplication.context.getSharedPreferences("xier_wangpan", Context.MODE_PRIVATE)

}