package com.example.quizapp.model

data class Question(
    val question: String,
    val options: List<String>,
    val answer: String
)
