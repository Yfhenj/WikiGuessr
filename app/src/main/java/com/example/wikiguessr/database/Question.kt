package com.example.wikiguessr.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preloaded_question_table")
data class Question(
    @PrimaryKey(autoGenerate = true)
    val questionID: Long = 0L,

    @ColumnInfo(name = "question_title")
    val title: String = "",

    @ColumnInfo(name = "question_text")
    var text: String = "",

    @ColumnInfo(name = "difficulty")
    val difficulty: Int = 1
)