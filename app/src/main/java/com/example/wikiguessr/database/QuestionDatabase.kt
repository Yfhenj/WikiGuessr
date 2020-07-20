package com.example.wikiguessr.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Question::class], version = 2)
abstract class QuestionDatabase: RoomDatabase(){
    abstract val questionDao: QuestionDao
}

private lateinit var INSTANCE: QuestionDatabase

fun getDatabase(context: Context): QuestionDatabase{
    synchronized(QuestionDatabase::class){
        if(!::INSTANCE.isInitialized){
            INSTANCE = Room
                .databaseBuilder(context.applicationContext, QuestionDatabase::class.java, "questions")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
    return INSTANCE
}