package com.example.xierlogintest.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.xierlogintest.R
import com.example.xierlogintest.XierWangpanApplication
import com.example.xierlogintest.dao.UserDao

import com.example.xierlogintest.model.User
import com.example.xierlogintest.network.service.RetrofitApiService
import com.example.xierlogintest.network.utils.UtilRetrofitCreator
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.et_username
import kotlinx.android.synthetic.main.activity_login.loginBtn
import kotlinx.android.synthetic.main.activity_signup.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class LoginActivity : ToolbarActivity(), View.OnClickListener {

    private val TAG = "LoginActivity"

    val srName by lazy { sr_name }

    val retrofitApiService by lazy { UtilRetrofitCreator.create(RetrofitApiService::class.java) }

    override fun setToolbar() {
        mToolbar.setTitle("登录")
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_login
    }

    override fun initView() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            100
        )//申请存储权限
        if (UserDao.isUserSaved()) {
            val user = UserDao.getSavedUser()
            tryLogin(user)
        }
        et_username!!.addTextChangedListener(mTextWatcher)
        loginBtn.setOnClickListener(this)
        et_signBtn.setOnClickListener(this)
        et_offlineLoginBtn.setOnClickListener(this)

    }

    private val mTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        val pattern = "^[A-Za-z0-9]+$"
        override fun afterTextChanged(s: Editable) {
            if (srName.editText!!.text.length > srName.counterMaxLength)
                srName.error = "输入内容超过上限"
            else if (srName.editText!!.text.length > 0 && !Pattern.matches(
                    pattern,
                    srName.editText!!.text
                )
            )
                srName.error = "用户名存在非法字符"
            else
                srName.error = null
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.loginBtn -> {
                val username = et_username.text.toString().trim()
                val password = et_password.text.toString().trim()
                val user = User(username, password)
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(v.context, "用户名或密码不可为空", Toast.LENGTH_SHORT).show()
                } else if (srName.error != null) {
                    Toast.makeText(v.context, "用户名格式错误", Toast.LENGTH_SHORT).show()
                } else {
                    tryLogin(user)
                }
            }

            R.id.et_signBtn -> {
                val intent = Intent(this, SignupActivity::class.java)
                startActivity(intent)
            }

            R.id.et_offlineLoginBtn -> {
                ActivityController.isOnline = false
                ActivityController.loginUser = null
                val loginIntent = Intent(this, MainActivity::class.java)
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(loginIntent)
            }
        }
    }

    private fun tryLogin(loginUser: User) {
        val loginIntent = Intent(this, MainActivity::class.java)
        val context = XierWangpanApplication.context
        Log.d(TAG, "loginUser = $loginUser")
        retrofitApiService.userLogin(loginUser).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                Log.d(TAG, "onResponse: ${response.toString()}")
                val responseUser = response.body()
                Log.d(TAG, "onResponse: responseHeaders = ${response.headers()}")
                Log.d(TAG, "responseUser = ${responseUser.toString()}")
                Log.d(TAG, "responseUser is User? = ${responseUser is User}")
                if (responseUser is User) {
                    Log.d(TAG, "onResponse: succeed")
                    val cookie = response.raw().header("Set-Cookie")
                    if (cookie != null && ActivityController.COOKIE != cookie) {
                        ActivityController.COOKIE = cookie
                    }
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    loginIntent.putExtra("parentFolderId", 0)
                    UserDao.saveUser(loginUser)
                    Log.d(TAG, "onResponse: ${responseUser}")
//                            loginIntent.putExtra("user",responseUser)
                    ActivityController.loginUser = responseUser
                    ActivityController.isOnline = true
                    startActivity(loginIntent)
                } else {
                    Log.d(TAG, "onResponse: ${response.body().toString()}")
                    Log.d(TAG, "onResponse: 用户名或密码错误")
                    Toast.makeText(context, "用户名或密码错误", Toast.LENGTH_SHORT).show()
                    et_username.text?.clear()
                    et_password.text?.clear()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(context, "您可能未连接上网络，请使用离线模式登录", Toast.LENGTH_SHORT)
                    .show()
                ActivityController.isOnline = false
                Log.d(TAG, "Failed")
            }
        })
    }
}




