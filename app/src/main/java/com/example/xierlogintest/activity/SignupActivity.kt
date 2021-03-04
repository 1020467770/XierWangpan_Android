package com.example.xierlogintest.activity

import android.app.Application
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.example.xierlogintest.R
import com.example.xierlogintest.model.User
import com.example.xierlogintest.network.service.RetrofitApiService
import com.example.xierlogintest.network.utils.UtilRetrofitCreator
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_signup.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : ToolbarActivity() {
    private val TAG = "SignupActivity"

    lateinit var srName2: TextInputLayout
    lateinit var retrofitApiService: RetrofitApiService

    override fun setToolbar() {
        mToolbar.setTitle("注册")
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_signup
    }

    override fun initView() {
        sp_username!!.addTextChangedListener(mTextWatcher)
        srName2 = sr2_name
        retrofitApiService = UtilRetrofitCreator.create(RetrofitApiService::class.java)
        sp_signupBtn.setOnClickListener {
            val loginIntent = Intent(this, MainActivity::class.java)
            val username = sp_username.text.toString().trim()
            val password = sp_password.text.toString().trim()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(it.context, "用户名或密码不可为空", Toast.LENGTH_SHORT).show()
            } else {
                retrofitApiService.userSignUp(
                    username, password
                ).enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        val responseUser = response.body()
                        if (responseUser is User) {
                            Log.d(TAG, "onResponse: succeed")
                            val cookie = response.raw().header("Set-Cookie")
                            if (cookie != null && ActivityController.COOKIE != cookie) {
                                ActivityController.COOKIE = cookie
                            }
                            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            loginIntent.putExtra("parentFolderId", 0)
//                        loginIntent.putExtra("user",responseUser)
                            Log.d(TAG, "onResponse返回的新注册的用户信息如下: ${responseUser}")
                            ActivityController.loginUser = responseUser
                            ActivityController.isOnline = true
                            startActivity(loginIntent)
                        } else {
                            Log.d(TAG, "onResponse: ${response.body().toString()}")
                            Log.d(TAG, "onResponse: 已经存在该用户")
                            Toast.makeText(it.context, "该用户名已存在", Toast.LENGTH_SHORT).show()
                            sp_username.text?.clear()
                            sp_password.text?.clear()
                        }
                        //startActivity(loginIntent)
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        t.printStackTrace()
                        Log.d(TAG, "Failed")
                        Toast.makeText(it.context, "您可能未连接上网络，请使用离线模式登录", Toast.LENGTH_SHORT).show()
                        ActivityController.isOnline = false
                    }

                })
            }
        }
    }

    private val mTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            if (srName2.editText!!.text.length > srName2.counterMaxLength)
                srName2.error = "输入内容超过上限"
            else
                srName2.error = null
        }
    }

}