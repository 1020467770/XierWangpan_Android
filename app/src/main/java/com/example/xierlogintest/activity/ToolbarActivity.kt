package com.example.xierlogintest.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.xierlogintest.R

abstract class ToolbarActivity : BaseActivity() {

    lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        mToolbar = findViewById(R.id.toolbar)

        mToolbar.setNavigationOnClickListener {
            finish()//后退键
        }

        setToolbar()
        initView()
    }

    protected abstract fun initView()

    protected abstract fun setToolbar()

    protected abstract fun getLayoutId(): Int


}