package com.example.xierlogintest.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(val username: String, val password: String): Parcelable {


    val id: Int = 0

    val container: Long = 0

    override fun toString(): String {
        return "User(username='$username', password='$password', id=$id, container=$container)"
    }


//    lateinit var list:List<BasicFile>

}