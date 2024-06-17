package com.example.quizapp.viewModel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.model.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class QuizViewModel(application: Application): AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    val questions = mutableStateOf<List<Question>>(emptyList())
    val currentQuestionIndex = mutableStateOf(0)
    val timer = mutableStateOf(600)
    val isQuizFinished = mutableStateOf(false)
    val userAnswers = mutableStateOf(mutableListOf<String?>())
    val userName = mutableStateOf("")
    val isQuizStarted = mutableStateOf(false)

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            val json = withContext(Dispatchers.IO) {
                context.assets.open("questions.json").bufferedReader().use { it.readText() }
            }
            val jsonArray = JSONArray(json)
            val questionList = mutableListOf<Question>()
            for(i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val question = Question(
                    question = item.getString("question"),
                    options = item.getJSONArray("options").let { optionsArray->
                        List(optionsArray.length()) { optionsArray.getString(it) }
                    },
                    answer = item.getString("answer")
                )
                questionList.add(question)
            }
            questions.value = questionList
            userAnswers.value = MutableList(questionList.size) { null }
        }
    }

    fun submitAnswer(answer: String?) {
        userAnswers.value[currentQuestionIndex.value] = answer
    }

    fun nextQuestion() {
        if (currentQuestionIndex.value < questions.value.size - 1) {
            currentQuestionIndex.value++
        } else {
            isQuizFinished.value = true
        }
    }

    fun calculateScore(): Int {
        var score = 0
        for (i in questions.value.indices) {
            if (questions.value[i].answer == userAnswers.value[i]) {
                score++
            }
        }
        return score
    }

    fun saveState() {
        if (currentQuestionIndex.value < questions.value.size - 1) {
            val sharedPref = context.getSharedPreferences("QuizApp", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putInt("currentQuestionIndex", currentQuestionIndex.value)
                putInt("timer", timer.value)
                putStringSet("userAnswers", userAnswers.value.map { it ?: "" }.toSet())
                putString("userName", userName.value)
                putBoolean("isQuizStarted", isQuizStarted.value)
                apply()

                Log.d("saveState", "saveState: ${sharedPref.all}")
            }
        }
    }

    fun loadState() {
        val sharedPref = context.getSharedPreferences("QuizApp", Context.MODE_PRIVATE)
        currentQuestionIndex.value = sharedPref.getInt("currentQuestionIndex", 0)
        timer.value = sharedPref.getInt("timer", 600)
        userAnswers.value = sharedPref.getStringSet("userAnswers", emptySet())?.map { if (it.isEmpty()) null else it }?.toMutableList() ?: MutableList(questions.value.size) { null }
        userName.value = sharedPref.getString("userName", "").toString()
        isQuizStarted.value = sharedPref.getBoolean("isQuizStarted", false)
    }

    fun resetQuiz() {
        currentQuestionIndex.value = 0
        timer.value = 600
        isQuizFinished.value = false
        userAnswers.value = MutableList(questions.value.size) { null }
        userName.value = ""
        isQuizStarted.value = false
    }
}