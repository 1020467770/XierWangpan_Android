package com.example.xierlogintest.utils

import com.example.xierlogintest.model.BasicFile

object MyFileUtils {

    fun getFileRealName(file: BasicFile): String {
        val fileName = file.fileName
        val start = fileName.lastIndexOf("@") + 1
        val name = fileName.substring(start)
        return name
    }

}