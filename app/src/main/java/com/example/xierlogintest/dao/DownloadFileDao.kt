package com.example.xierlogintest.dao

import androidx.room.*

import com.example.xierlogintest.model.DownloadFile

@Dao
interface DownloadFileDao {

    @Insert
    fun insertDownloadFile(file: DownloadFile): Long

    @Query("select * from DownloadFile")
    fun loadAllDownloadFiles():List<DownloadFile>

    @Update
    fun updateDownloadFile(newFile:DownloadFile)

    @Delete
    fun deleteDownloadFile(file:DownloadFile)

}