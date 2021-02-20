package com.example.xierlogintest.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.xierlogintest.R
import com.example.xierlogintest.network.service.RetrofitApiService
import com.example.xierlogintest.network.utils.UtilRetrofitCreator
import kotlinx.android.synthetic.main.activity_upload.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody

import java.io.File
import java.lang.Exception
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


//测试用Activity，在项目中没有用到
class UploadActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var editTextName: EditText
    private lateinit var prgDialog: ProgressDialog

    private val RESULT_LOAD_IMG = 1
    private val encodedString: String? = null
    private val bitmap: Bitmap? = null
    private lateinit var imgPath: String
    private lateinit var service: RetrofitApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        prgDialog = (ProgressDialog(this))
        prgDialog.setCancelable(false)


        service = UtilRetrofitCreator.create(RetrofitApiService::class.java)
        editTextName = editText
        choose_image.setOnClickListener(this)
        upload_image.setOnClickListener(this)
        createFolderBtn.setOnClickListener(this)
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限*/
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            100
        )
        //}

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.choose_image -> {
                loadImag()
            }
            R.id.upload_image -> {
                uploadImag()
            }
            R.id.createFolderBtn -> {
                createNewFolder()
            }
        }
    }

    private fun createNewFolder() {
        service.createNewFolder("我的文件夹", ActivityController.loginUser!!.username, 0)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Log.d(TAG, "onResponse: succeed")
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.printStackTrace()
                    Log.d(TAG, "onFailure: failed")
                }
            })

    }

    private fun loadImag() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null) {
                val selectedImage: Uri? = data.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                //获取游标
                val cursor: Cursor? =
                    contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
                cursor?.moveToFirst()

                val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                imgPath = cursor!!.getString(columnIndex!!)
                /*Toast.makeText(
                    this, "You haven't picked Image ${imgPath}",
                    Toast.LENGTH_LONG
                ).show();*/
//                Log.d(TAG, "onActivityResult: ${imgPath}")
                cursor?.close()
//                imageView.setImageBitmap(BitmapFactory.decodeFile(imgPath))

            } else {
                Toast.makeText(
                    this, "You haven't picked Image",
                    Toast.LENGTH_LONG
                ).show();
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val TAG = "UploadActivity"

    private fun uploadImag() {
        TextUtils.isEmpty(imgPath)
        if (imgPath != null && imgPath.isNotEmpty()) {
            /*prgDialog.setMessage("Converting Image to Binary Data")
            prgDialog.show()*/

            val file = File(imgPath)

            Log.d(TAG, "uploadImag: ${file}")

            val requestBody: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), file)

            val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
            val formatDate = sdf.format(Date())
//            val formatDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
//            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置北京时区
            Log.d(TAG, "uploadImag: ${formatDate}")
//            Log.d(TAG, "uploadImag: ${Date().toString()}")

            service.uploadPicture(
                "${ActivityController.loginUser!!.username}",
                formatDate.toString(),
                0,
                body
            )
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        Log.d(TAG, "onResponse: succeed")
                        Log.d(TAG, "onResponse: ${body}")
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        t.printStackTrace()
                        Log.d(TAG, "onResponse: failed")

                    }
                })

            Log.d(TAG, "uploadImag: ${imgPath}")

        } else {
            Log.d(TAG, "uploadImag: 路径为空")
        }
    }


}