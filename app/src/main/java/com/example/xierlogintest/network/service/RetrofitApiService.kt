package com.example.xierlogintest.network.service

import com.example.xierlogintest.model.BasicFile
import com.example.xierlogintest.model.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface RetrofitApiService {

    @POST("loginServlet")
    fun userLogin(@Body loginUser: User): Call<User>

    @POST("signUpServlet")
    fun userSignUp(
        @Query("username") username: String,
        @Query("password") password: String
    ): Call<User>

    @Multipart
    @POST("uploadFileServlet")
    fun uploadPicture(
        @Query("uploader") uploader: String,
        @Query("uploadDate") uploadDate: String,
        @Query("parentFolderId") parentFolderId: Int,
        @Part imgs: MultipartBody.Part
    ): Call<ResponseBody>

    @POST("createNewFolderServlet")
    fun createNewFolder(
        @Query("folderName") folderName: String,
        @Query("creatorUsername") creatorUsername: String,
        @Query("parentFolderId") parentFolderId: Int
    ): Call<ResponseBody>

    @GET("getFilesByParentIdServlet")
    fun getCurrentPageFiles(
        @Query("userId") userId: Int,
        @Query("parentFolderId") parentFolderId: Int
    ): Call<List<BasicFile>>

    @GET("downloadServlet")
    fun downloadFile(
        @Query("fileName") fileName: String
    ): Call<ResponseBody>

}