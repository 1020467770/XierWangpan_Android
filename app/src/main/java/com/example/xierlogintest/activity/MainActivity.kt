package com.example.xierlogintest.activity

import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.xierlogintest.R
import com.example.xierlogintest.ViewModel.MainViewModel
import com.example.xierlogintest.adapter.FileAdapter
import com.example.xierlogintest.dao.UserDao
import com.example.xierlogintest.database.AppDatabase
import com.example.xierlogintest.dialog.UploadBottomSheetDialog
import com.example.xierlogintest.model.BasicFile
import com.example.xierlogintest.network.service.RetrofitApiService
import com.example.xierlogintest.network.utils.UtilRetrofitCreator
import com.example.xierlogintest.utils.MyFileUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ToolbarActivity(), NavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener {

    private val TAG = "MainActivity"

    var fileList: ArrayList<BasicFile> = ArrayList()

    lateinit var adapter: FileAdapter
    lateinit var retrofitApiService: RetrofitApiService
    lateinit var viewModel: MainViewModel

    var currentFolderId: Int = 0
    var beforeFolderId: Int = 0

    override fun onDestroy() {
        super.onDestroy()
        ActivityController.currentFolderId = beforeFolderId
    }

    override fun initView() {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        setSupportActionBar(mToolbar)//必须先写
        initMenu()

        beforeFolderId = ActivityController.currentFolderId
        currentFolderId = intent.getIntExtra("parentFolderId", 0)
        ActivityController.currentFolderId = currentFolderId
//        initRecyclerView()
        if (ActivityController.isOnline == true) {
            Log.d(TAG, "initView: 有网络模式")
            initRecyclerViewList()
        } else {
            Log.d(TAG, "initView: 无网络模式")
            initRecyclerViewList_offline()
        }
        initSearchView()

        floating_button_left.setOnClickListener(this)
        floating_button_right.setOnClickListener(this)

    }

    private fun initRecyclerViewList_offline() {
        val filedao = AppDatabase.getDatabase(this).downloadFileDao()
        val loadAllDownloadFile = filedao.loadAllDownloadFiles()//这里是因为允许在主线程中访问数据库
        fileList = loadAllDownloadFile as ArrayList<BasicFile>
        Log.d(TAG, "initRecyclerViewList_offline: ${fileList}")
        Log.d(TAG, "fileList的长度是： ${fileList.size}")
        viewModel.fileList = fileList
        val layoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = layoutManager
        if (!::adapter.isInitialized) {
            adapter = FileAdapter(fileList)
        }
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //引用menu
        menuInflater.inflate(R.menu.menu_search, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val mSearchView = searchItem.actionView as SearchView

        //设置搜索图标是否显示在搜索框内
        mSearchView.setIconifiedByDefault(true)

        //设置搜索框展开时是否显示提交按钮
        mSearchView.isSubmitButtonEnabled = true

        //键盘的回车设置成搜索
        mSearchView.imeOptions = EditorInfo.IME_ACTION_SEARCH

        //搜索框初始是否展开, false表示展开
        mSearchView.isIconified = true

        //获取焦点
        mSearchView.isFocusable = true
        mSearchView.requestFocusFromTouch()

        //设置提示词
        mSearchView.queryHint = "请输入关键字"

        //设置输入框文字颜色和大小
        val editText = mSearchView.findViewById<EditText>(R.id.search_src_text)//这个是系统默认的输入框
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, resources.getDimension(R.dimen.edit_text))
        editText.setHintTextColor(ContextCompat.getColor(this, R.color.gray))
        editText.setTextColor(ContextCompat.getColor(this, R.color.white))

        //设置搜索文本监听
        val context = this
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            //当内容不为空，点击搜索按钮时触发
            override fun onQueryTextSubmit(query: String): Boolean {
                Snackbar.make(constraint_layout, "搜索内容为$query", Snackbar.LENGTH_SHORT).show()

                val newFileList = fileList.filter {
                    MyFileUtils.getFileRealName(it).contains(query)
                }

                val anotherAdapter = FileAdapter(newFileList)
                recyclerView.adapter = anotherAdapter
                //搜索内容
                //清除焦点，收软键盘
                mSearchView.clearFocus();
                return false
            }

            //当搜索内容改变时触发该方法
            override fun onQueryTextChange(newText: String): Boolean {
                /*if (TextUtils.isEmpty(newText)) search_result!!.visibility = View.INVISIBLE*/
//                Snackbar.make(constraint_layout, "搜索内容变了", Snackbar.LENGTH_SHORT).show()
                val newFileList = fileList.filter {
                    MyFileUtils.getFileRealName(it).contains(newText)
                }

                val anotherAdapter = FileAdapter(newFileList)
                recyclerView.adapter = anotherAdapter
                return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun initSearchView() {
        mToolbar.contentInsetStartWithNavigation = 0
//        setSupportActionBar(mToolbar)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: 执行了Pause")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: 执行了Start")
        if (::viewModel.isInitialized) {
            fileList = viewModel.fileList as ArrayList<BasicFile>
        }
        updateContainView()

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: 执行了Resume")
    }

    private fun initRecyclerViewList() {
        retrofitApiService = UtilRetrofitCreator.create(RetrofitApiService::class.java)
        val context = this
        retrofitApiService.getCurrentPageFiles(
            ActivityController.loginUser!!.id,
            currentFolderId
        ).enqueue(object : Callback<List<BasicFile>> {
            override fun onResponse(
                call: Call<List<BasicFile>>,
                response: Response<List<BasicFile>>
            ) {
                Log.d(TAG, "onResponse: succeed")
                if (response.body() != null) {
                    fileList = response.body() as ArrayList<BasicFile>
                    viewModel.fileList = fileList
                }

                val layoutManager = GridLayoutManager(context, 3)
                recyclerView.layoutManager = layoutManager
                if (!::adapter.isInitialized) {
                    adapter = FileAdapter(fileList)
                }
                recyclerView.adapter = adapter

                for (basicFile in fileList) {
                    Log.d(TAG, "onResponse: ${basicFile.toString()}")
                }
            }

            override fun onFailure(call: Call<List<BasicFile>>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onFailure: failed")
            }
        })
    }

    private fun initRecyclerView() {//测试用

    }

    private fun initMenu() {
        //关联mToolbar
        val actionBarDrawerToggle =
            ActionBarDrawerToggle(this, drawer_layout, mToolbar, R.string.open, R.string.close)
        actionBarDrawerToggle.syncState()//初始化
        drawer_layout.addDrawerListener(actionBarDrawerToggle)//给布局增添抽屉式菜单按钮监听器

        //debug监听
        drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                Log.d(TAG, "滑动中")
            }

            override fun onDrawerOpened(drawerView: View) {
                Log.d(TAG, "打开")
            }

            override fun onDrawerClosed(drawerView: View) {
                Log.d(TAG, "关闭")
            }

            override fun onDrawerStateChanged(newState: Int) {
                Log.d(TAG, "状态改变")
            }
        })

        //内容点击
        navigation_view.setNavigationItemSelectedListener(this)

        updateContainView()

    }

    private fun updateContainView() {
        val headerView = navigation_view.getHeaderView(0)
        val tv_user_leftdrawer: TextView = headerView.findViewById(R.id.drawer_left_username)
        val tv_user_desc_leftdrawer: TextView = headerView.findViewById(R.id.drawer_left_user_desc)
//        updateContainView()
        ActivityController.loginUser?.let {
            var currentContain = it.currentContain
            var currentString: String = ""
            if (currentContain / 1024 / 1024 >= 1) {
                currentContain = currentContain / 1024 / 1024
                currentString = "${currentContain}MB"
            } else {
                currentContain = currentContain / 1024
                currentString = "${currentContain}KB"
            }
            tv_user_leftdrawer.text = it.username.toString()
            tv_user_desc_leftdrawer.text =
                "${currentString}/${it.container / 1024 / 1024}MB"
        }
    }

    override fun setToolbar() {
        if (ActivityController.isOnline == true) {
            mToolbar.setTitle("个人网盘")
        } else {
            mToolbar.setTitle("本地网盘")
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val title = item.title as String
        Toast.makeText(this, "点击了$title", Toast.LENGTH_SHORT).show()
        when (item.itemId) {
            R.id.nav_exitLogin -> {
                val exitIntent = Intent(this, LoginActivity::class.java)
                exitIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                exitIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ActivityController.loginUser = null
                ActivityController.COOKIE = ""
                UserDao.removeUser()
                startActivity(exitIntent)
            }
        }
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.floating_button_left -> {
                finish()
            }
            R.id.floating_button_right -> {
                recyclerView.adapter = adapter
                UploadBottomSheetDialog().show(supportFragmentManager, "UploadBottomSheetDialog")
            }
        }
    }

    public fun refreshRecyclerView() {
        updateContainView()
        Log.d(TAG, "refreshRecyclerView: 执行刷新了")
        retrofitApiService.getCurrentPageFiles(
            ActivityController.loginUser!!.id,
            currentFolderId
        ).enqueue(object : Callback<List<BasicFile>> {
            override fun onResponse(
                call: Call<List<BasicFile>>,
                response: Response<List<BasicFile>>
            ) {
                Log.d(TAG, "onRefreshResponse: succeed")
                var newFileList: ArrayList<BasicFile> = ArrayList()
                if (response.body() != null) {
                    newFileList = response.body() as ArrayList<BasicFile>
                }
                if (newFileList.isNotEmpty() && newFileList.size > fileList.size) {
                    Log.d(TAG, "onRefreshResume: 返回的list比原来的大")
//                    fileList.add(ImgFile())
                    fileList.clear()
                    fileList.addAll(newFileList)
                    viewModel.fileList = fileList
                    adapter.notifyItemInserted(fileList.size - 1)//因为定义的MsgAdapter是非空的，所以不用?.
                    recyclerView.scrollToPosition(fileList.size - 1)//将RecyclerView定位到最后一行
                }
            }

            override fun onFailure(call: Call<List<BasicFile>>, t: Throwable) {
                t.printStackTrace()
                Log.d(TAG, "onRefreshFailure: failed")
            }
        })
    }
}