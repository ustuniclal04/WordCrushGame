package com.example.wordcrushgame

data class GameResult(
    val gameNumber: Int,   // 🔥 EN ÜSTE AL
    val date: String,
    val gridSize: Int,
    val score: Int,
    val wordCount: Int,
    val longestWord: String,
    val duration: Int
)
