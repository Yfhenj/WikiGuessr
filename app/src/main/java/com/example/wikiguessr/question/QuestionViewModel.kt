package com.example.wikiguessr.question

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.wikiguessr.database.Question
import com.example.wikiguessr.database.getDatabase
import com.example.wikiguessr.network.searchUrl
import com.example.wikiguessr.repository.QuestionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.lang.StringBuilder

class QuestionViewModel(app: Application) : ViewModel() {

    private val uiScope = CoroutineScope(Dispatchers.Main)

    private val database = getDatabase(app)
    val repository = QuestionRepository(database)

    private val _wikiText = MutableLiveData<String>()
    val wikiText: LiveData<String>
        get() = _wikiText

    private val _noInternetSnackbarEvent = MutableLiveData<Boolean>()
    val noInternetSnackbarEvent: LiveData<Boolean>
        get() = _noInternetSnackbarEvent

    var noInternet = false

    fun noInternetSnackbarEventDone() {
        _noInternetSnackbarEvent.value = false
        noInternet = true
    }

    init {
        repository.preloadedQuestions.observeForever {
            if (it.size <= 10 || it == null) {
                getWikiPage()
                Log.i("MyTags", "${it.size}")
                if (_wikiText.value == "loading..") onGetPageClicked()
            }
        }
        onGetPageClicked()
    }

    fun makeQuestion(question: Question): String {
        val wikiResult = question.text
        val wikiTitle = question.title
        var questionBuf = wikiResult
        var inBracket = false
        for (i in wikiResult.withIndex()) {
            if (i.value == '—' && !inBracket) {
                questionBuf = wikiResult.substring(startIndex = i.index)
                break
            }
            if (i.value == '(') inBracket = true
            if (i.value == ')') inBracket = false
        }
        val resultQuestion = questionBuf.replace(wikiTitle, "<...>", true)
        return if (wikiTitle.length > 3) resultQuestion.replace(
            wikiTitle.dropLast(1),
            "<...>",
            true
        ) else resultQuestion
    }

    var currentQuestion: Question? = null

    fun onGetPageClicked() {
        uiScope.launch {
            if (noInternet) {
                getWikiPage()
            }
            _wikiText.value = "loading.."
            currentQuestion = repository.getQuestion()
            currentQuestion?.let {
                _wikiText.value = makeQuestion(it)
                repository.delete(it.questionID)
            }
        }
    }

    fun onAnswerClicked() {
        currentQuestion?.let {
            _wikiText.value = it.text
        }
    }

    suspend fun getAnswer(): String {
        var answer: String
        var questionNumber: Long
        do {
            val doc = repository.getRandomAnswer()
            val docselect = doc.select("p")
            Log.i("MyTags", docselect.text())
            answer = docselect.find { it.text().contains("Ответ:") }
                ?.text()
                ?.removePrefix("Ответ: ")
                ?.removeSuffix(".").toString()
//                .filter { it.text().contains("Ответ:") }
//                .forEach { answers.append(it.text().removePrefix("Ответ: ").removeSuffix(".") + "\n") }
            Log.i("MyTags", answer)
            questionNumber = repository.getAnswerSearch(answer)
                .select("h2.title")
                .text()
                .dropWhile { it == '.' }
                .filter { it.isDigit() }
                .toLong()
        } while (questionNumber !in 1..50)
        return answer
    }


    fun getWikiPage() {
        uiScope.launch {
            try {
                var doc = repository.getWikiPage(getAnswer())
                var header = getHeader(doc)
//            var i = 0
                while (doc.title().contains("Поиск") || doc.title().contains("Значения") || header.length < 100 || header.length > 1000) {
                    doc = repository.getWikiPage(getAnswer())
                    header = getHeader(doc)
//                Log.i("Mytags", "$i")
//                i++
                }
                val title = doc.getElementById("firstHeading").text()
                val result = title + header
                val question = Question(title = title, text = result)
                repository.saveQuestion(question)
                noInternet = false
            } catch (e: IOException) {
                _noInternetSnackbarEvent.value = true
            }
        }
    }

    fun getHeader(doc: Document): String {
        val tags = doc.allElements
        val result = StringBuilder()
        for (it in tags.withIndex()) {
            if (it.value.tagName() != "h2") {
                if (it.value.tagName() == "p" && tags[it.index - 1].tagName() != "td")
                    result.append("\n\n" + it.value.text())
            } else break
        }
        return result.toString()
    }
}

