package com.example.xierlogintest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.xierlogintest.dao.DownloadFileDao
import com.example.xierlogintest.model.DownloadFile


@Database(version = 1, entities = [DownloadFile::class])
abstract class AppDatabase : RoomDatabase() {//必须定义成抽象的类

    abstract fun downloadFileDao(): DownloadFileDao

    companion object {

        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                context.applicationContext,//第一个参数必须使用applicationContext上下文，否则容易出现内存泄漏
                AppDatabase::class.java, "app_database"
            )
                .allowMainThreadQueries()
//                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build().apply {
                    instance = this
                }
        }

    }
}