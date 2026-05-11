package com.example.wordcrushgame

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.roundToInt

class ScoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_score)

            val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewScores)
            val btnBack = findViewById<TextView>(R.id.btnBack)

            // 🔥 İSTATİSTİK TEXTLERİ
            val tvTotalGames = findViewById<TextView>(R.id.tvTotalGames)
            val tvHighScore = findViewById<TextView>(R.id.tvHighScore)
            val tvAvgScore = findViewById<TextView>(R.id.tvAvgScore)
            val tvTotalWords = findViewById<TextView>(R.id.tvTotalWords)
            val tvLongestWord = findViewById<TextView>(R.id.tvLongestWord)
            val tvTotalTime = findViewById<TextView>(R.id.tvTotalTime)

            btnBack?.setOnClickListener { finish() }

            val prefs = getSharedPreferences("GAME_DATA", MODE_PRIVATE)
            val gson = Gson()

            val json = prefs.getString("RESULTS", null)
            val type = object : TypeToken<MutableList<GameResult>>() {}.type

            val list: MutableList<GameResult> =
                if (json.isNullOrEmpty()) {
                    mutableListOf()
                } else {
                    try {
                        gson.fromJson(json, type) ?: mutableListOf()
                    } catch (e: Exception) {
                        Log.e("SCORE_ERROR", "JSON bozuk → temizlendi", e)
                        prefs.edit().clear().apply()
                        mutableListOf()
                    }
                }

            // 🔥 NUMARALANDIRMA (EN ESKİ = 1)
            val size = list.size
            for (i in list.indices) {
                list[i] = list[i].copy(gameNumber = size - i)
            }

            // 🔥 İSTATİSTİK HESAPLAMA
            if (list.isNotEmpty()) {

                val totalGames = list.size
                val highScore = list.maxOf { it.score }
                val avgScore = list.map { it.score }.average().roundToInt()
                val totalWords = list.sumOf { it.wordCount }
                val longestWord =
                    list.maxByOrNull { it.longestWord.length }?.longestWord ?: "-"

                val totalTimeSec = list.sumOf { it.duration }
                val minutes = totalTimeSec / 60
                val seconds = totalTimeSec % 60

                // 🔥 EKRANA BAS
                tvTotalGames.text = "Toplam Oyun: $totalGames"
                tvHighScore.text = "En Yüksek Puan: $highScore"
                tvAvgScore.text = "Ortalama Puan: $avgScore"
                tvTotalWords.text = "Toplam Kelime: $totalWords"
                tvLongestWord.text = "En Uzun Kelime: $longestWord"
                tvTotalTime.text = "Toplam Süre: ${minutes} dk ${seconds} sn"

            } else {
                // 🔴 BOŞ DURUM
                tvTotalGames.text = "Toplam Oyun: 0"
                tvHighScore.text = "En Yüksek Puan: 0"
                tvAvgScore.text = "Ortalama Puan: 0"
                tvTotalWords.text = "Toplam Kelime: 0"
                tvLongestWord.text = "En Uzun Kelime: -"
                tvTotalTime.text = "Toplam Süre: 0 sn"
            }

            // 🔥 GÜNCEL VERİYİ KAYDET
            prefs.edit().putString("RESULTS", gson.toJson(list)).apply()

            Log.d("SCORE_DEBUG", gson.toJson(list))

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = ScoreAdapter(list)

        } catch (e: Exception) {
            Log.e("CRASH", "ScoreActivity çöktü", e)
            Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}