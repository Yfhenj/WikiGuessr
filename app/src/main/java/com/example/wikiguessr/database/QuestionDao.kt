package com.example.wikiguessr.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface QuestionDao{

    @Insert
    fun insert(question: Question)

    @Query("SELECT * FROM preloaded_question_table ORDER BY questionID LIMIT 1")
    fun getQuestion(): Question?

    @Query("SELECT * FROM preloaded_question_table ORDER BY questionID")
    fun getAllQuestions(): LiveData<List<Question>>

    @Query("DELETE FROM preloaded_question_table WHERE questionID = :key")
    fun delete(key: Long)

    @Query("DELETE FROM preloaded_question_table")
    fun clear()

}