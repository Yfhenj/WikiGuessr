package com.example.wikiguessr.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.wikiguessr.database.Question
import com.example.wikiguessr.database.QuestionDatabase
import com.example.wikiguessr.network.searchUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class QuestionRepository(private val database: QuestionDatabase) {

    private val uiScope = CoroutineScope(Dispatchers.Main)

    val preloadedQuestions: LiveData<List<Question>> = database.questionDao.getAllQuestions()

    suspend fun getRandomAnswer(): Document {
        return withContext(Dispatchers.IO) {
            return@withContext Jsoup.connect("https://db.chgk.info/random").get()
        }
    }

    suspend fun getWikiPage(answer: String): Document {
        return withContext(Dispatchers.IO) {
            return@withContext Jsoup.connect(searchUrl + answer).get()
        }
    }

    suspend fun getAnswerSearch(answer: String): Document {
        return withContext(Dispatchers.IO) {
            return@withContext Jsoup.connect("https://db.chgk.info/search/questions/$answer").get()
        }
    }

    suspend fun saveQuestion(question: Question){
        withContext(Dispatchers.IO){
            database.questionDao.insert(question)
        }
    }

    suspend fun getQuestion(): Question? {
        return withContext(Dispatchers.IO){
            return@withContext database.questionDao.getQuestion()
        }
    }

    suspend fun delete(key: Long){
        return withContext(Dispatchers.IO){
            return@withContext database.questionDao.delete(key)
        }
    }

    suspend fun clear(){
        return withContext(Dispatchers.IO){
            return@withContext database.questionDao.clear()
        }
    }
}