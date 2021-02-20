package com.example.xierlogintest.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.xierlogintest.R
import com.example.xierlogintest.activity.ActivityController
import com.example.xierlogintest.activity.MainActivity
import com.example.xierlogintest.network.service.RetrofitApiService
import com.example.xierlogintest.network.utils.UtilRetrofitCreator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_upload_bottom.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class UploadBottomSheetDialog : BottomSheetDialogFragment(), View.OnClickListener {

    private val RESULT_LOAD_IMG = 1

    //    private lateinit var imgPath: String
    private lateinit var service: RetrofitApiService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_upload_bottom, null)
        dialog.setContentView(view)
        initView(view)
        return dialog
    }

    private fun initView(rootView: View) {
        rootView.tv_cancel.setOnClickListener {
            dismiss()
        }
        rootView.bottomDialog_left_LL.setOnClickListener(this)
        rootView.bottomDialog_right_LL.setOnClickListener(this)
        service = UtilRetrofitCreator.create(RetrofitApiService::class.java)
    }

    override fun onStart() {
        super.onStart()
//        //拿到系统的 bottom_sheet
        val view: FrameLayout = dialog?.findViewById(R.id.design_bottom_sheet)!!
//        //获取behavior
        val behavior = BottomSheetBehavior.from(view)
//        //设置弹出高度
        behavior.peekHeight = 350
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.d(TAG, "onDismiss: Dismissed")
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Log.d(TAG, "onCancel: Canceled")
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bottomDialog_left_LL -> {
                if (ActivityController.isOnline == false) {
                    Toast.makeText(context, "您当前处于无网络状态哦，请连接网络后再试", Toast.LENGTH_SHORT).show()
                } else {
//                Toast.makeText(context, "you clicked 新建文件夹", Toast.LENGTH_SHORT).show()
                    val v = layoutInflater.inflate(R.layout.dialog_edittext_newfolder_name, null)
                    val et: EditText = v.findViewById(R.id.edittext_foldername_dialog)
                    AlertDialog.Builder(context).setTitle("")
                        .setIcon(null)
                        .setView(v)
                        .setPositiveButton("确定", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                Toast.makeText(
                                    context,
                                    "新创建的文件夹名字是${et.text.toString().trim()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val folderName = et.text.toString().trim()
                                if (folderName.isNotEmpty()) {
                                    createNewFolder(folderName)
                                }
                            }

                            private fun createNewFolder(fileName: String) {
                                service.createNewFolder(
                                    fileName,
                                    ActivityController.loginUser!!.username,
                                    ActivityController.currentFolderId
                                ).enqueue(object : Callback<ResponseBody> {
                                    override fun onResponse(
                                        call: Call<ResponseBody>,
                                        response: Response<ResponseBody>
                                    ) {
                                        Log.d(TAG, "onResponse: succeed")
                                        updateRecyclerview()
                                    }

                                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                        t.printStackTrace()
                                        Log.d(TAG, "onFailure: failed")
                                    }
                                })

                            }

                        }).setNegativeButton("取消", null).show()
                }

            }

            R.id.bottomDialog_right_LL -> {
//                Toast.makeText(context, "you clicked 上传本地图片", Toast.LENGTH_SHORT).show()
                if (ActivityController.isOnline == false) {
                    Toast.makeText(context, "您当前处于无网络状态哦，请连接网络后再试", Toast.LENGTH_SHORT).show()
                } else {
                    loadImg()
                }
            }
        }
    }

    private fun updateRecyclerview() {
        if (activity != null) {
            val mainActivity = activity as MainActivity
            mainActivity.refreshRecyclerView()

        }
    }

    private val TAG = "UploadBottomSheetDialog"


    private fun loadImg() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == RESULT_LOAD_IMG && resultCode == AppCompatActivity.RESULT_OK && data != null) {
                val selectedImage: Uri? = data.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                //获取游标
                val cursor: Cursor? =
                    context!!.contentResolver.query(
                        selectedImage!!,
                        filePathColumn,
                        null,
                        null,
                        null
                    )
                cursor?.moveToFirst()

                val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                val imgPath = cursor!!.getString(columnIndex!!)
                cursor?.close()
                uploadImg(imgPath)
            } else {
                Toast.makeText(
                    context, "您没有选择任何图片",
                    Toast.LENGTH_LONG
                ).show();
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun uploadImg(imgPath: String) {
//        TextUtils.isEmpty(imgPath)
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
                ActivityController.currentFolderId,
                body
            )
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        Log.d(TAG, "onResponse: succeed")
                        Log.d(TAG, "onResponse: ${body}")
                        updateRecyclerview()
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