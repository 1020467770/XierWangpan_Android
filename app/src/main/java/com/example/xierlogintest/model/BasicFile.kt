package com.example.xierlogintest.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.xierlogintest.R
import java.sql.Timestamp
import java.util.*

open class BasicFile {

    var fileName: String = "文件名"
    var creatorId: Int = 0
    var createTime: String = ""
    var capacity: Long = 0
    var fileRealPath: String = ""
    var idFile: Int = 0
    var parentId: Int = 0
    var fileType: Int = 0
    var imgId: Int = R.drawable.folder_64


    override fun toString(): String {
        return "BasicFile(fileName='$fileName', creatorId=$creatorId, createTime='$createTime', capacity=$capacity, fileRealPath='$fileRealPath', idFile=$idFile, parentId=$parentId, fileType=$fileType, imgId=$imgId)"
    }

    companion object {
        val FILETYPE_FOLDER = 0
        val FILETYPE_PICTURE = 1
    }



}