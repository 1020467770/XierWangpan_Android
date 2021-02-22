package com.example.xierlogintest.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.xierlogintest.R
import com.example.xierlogintest.activity.ActivityController
import com.example.xierlogintest.activity.MainActivity
import com.example.xierlogintest.adapter.viewHolder.FileViewHolder
import com.example.xierlogintest.dao.DownloadFileDao
import com.example.xierlogintest.database.AppDatabase
import com.example.xierlogintest.model.BasicFile
import com.example.xierlogintest.model.DownloadFile
import com.example.xierlogintest.network.service.RetrofitApiService
import com.example.xierlogintest.network.utils.UtilRetrofitCreator
import com.example.xierlogintest.utils.MyFileUtils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*
import kotlin.concurrent.thread


class FileAdapter(val fileList: List<BasicFile>) : RecyclerView.Adapter<FileViewHolder>() {

    private lateinit var fileDao: DownloadFileDao

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.basefile_item_layout, parent, false)
        val viewHolder = FileViewHolder(view)
        if(!::fileDao.isInitialized){
            fileDao = AppDatabase.getDatabase(parent.context).downloadFileDao()
        }

        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            Log.d(TAG, "onCreateViewHolder: ${position}")
            val file = fileList[position]
            Log.d(TAG, "onCreateViewHolder: ${file}")

            when (file.fileType) {
                BasicFile.FILETYPE_FOLDER -> {
                    openFolder(parent, file)
                }
                BasicFile.FILETYPE_PICTURE -> {
                    if(ActivityController.isOnline) {
                        downloadPicture(parent, file)
                    }else{
                        Toast.makeText(parent.context, "没有网络，点了没用", Toast.LENGTH_SHORT).show()
                    }
//                    Toast.makeText(parent.context, "you clicked a picture", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return viewHolder
    }

    private fun downloadPicture(parent: ViewGroup, file: BasicFile) {
        val context = parent.context
        val retrofitApiService = UtilRetrofitCreator.create(RetrofitApiService::class.java)
        retrofitApiService.downloadFile(file.fileName).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d(TAG, "onResponse: succeed")
                Log.d(TAG, "onResponse: ${response.body()}")
                val responseBody = response.body()
                if (response != null && responseBody != null) {
                    if (writeToDisk(responseBody) == true) {
                        Toast.makeText(context, "下载完成", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            private fun writeToDisk(body: ResponseBody): Boolean {
                var outPutStream: OutputStream? = null
                var inPutStream: InputStream? = null
                try {
                    outPutStream = context.openFileOutput(file.fileName, Context.MODE_PRIVATE)
                    Log.d(TAG, "writeToDisk: ${body.byteStream()}")
                    inPutStream = body.byteStream()
                    val fileReader = ByteArray(1024)
                    val fileSize = body.contentLength()
                    var len = 0
                    var fileSizeDownloaded = 0
                    while (true) {
                        len = inPutStream.read(fileReader)
                        if (len == -1) {
                            break
                        }
                        outPutStream.write(fileReader, 0, len)
                        fileSizeDownloaded += len;
//                        Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                    }
                    outPutStream.flush()

//                    val downloadFile = file

//                    Log.d(TAG, "writeToDisk:加入room数据库的文件信息如下： ${downloadFile}")
//                    downloadFile.downloadFileId = fileDao.insertDownloadFile(downloadFile)//往Room数据库里加上带有下载时间和id的下载文件，路径已经包括在文件名里
                    thread {
                        val downloadFile = DownloadFile(file)
                        downloadFile.downloadTime = Date().time
                        downloadFile.downloadFileId =
                            fileDao.insertDownloadFile(downloadFile)//往Room数据库里加上带有下载时间和id的下载文件，路径已经包括在文件名里
                        Log.d(TAG, "writeToDisk:加入room数据库的文件信息如下： ${downloadFile}")
                    }
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                    return false
                } finally {
                    if (inPutStream != null) {
                        inPutStream.close()
                    }
                    if (outPutStream != null) {
                        outPutStream.close()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(TAG, "onFailure: failed")
            }
        })

    }

    private val TAG = "FileAdapter"

    private fun openFolder(parent: ViewGroup, folder: BasicFile) {
        val nextIntent = Intent(parent.context, MainActivity::class.java)
        Log.d(TAG, "openFolder: 打开新的文件夹执行了")
        nextIntent.putExtra("parentFolderId", folder.idFile)
        parent.context.startActivity(nextIntent)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = fileList[position]

        if (file.fileType == BasicFile.FILETYPE_PICTURE) {
            val Uri = "http://39.97.183.4:8080/TESTS/files/" + file.fileName
            Glide
                .with(holder.iconImage)
                .load(Uri)
                .into(holder.iconImage)

        }
        holder.iconImage.setImageResource(file.imgId)
        holder.fileName.text = MyFileUtils.getFileRealName(file)
    }

    override fun getItemCount(): Int {
        return fileList.size
    }
}