package com.example.xierlogintest.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class DownloadFile() : BasicFile() {

    @PrimaryKey(autoGenerate = true)
    var downloadFileId: Long = 0

    var downloadTime: Long = 0 //存的是Date的毫秒数


    constructor(file: BasicFile) : this() {
        this.fileName = file.fileName
        this.creatorId = file.creatorId
        this.createTime = file.createTime
        this.capacity = file.capacity
        this.fileRealPath = file.fileRealPath
        this.idFile = file.idFile
        this.parentId = file.parentId
        this.fileType = file.fileType
        this.imgId = file.imgId
    }

    override fun toString(): String {
        return super.toString() + "DownloadFile(downloadFileId=$downloadFileId, downloadTime=$downloadTime)"
    }


}