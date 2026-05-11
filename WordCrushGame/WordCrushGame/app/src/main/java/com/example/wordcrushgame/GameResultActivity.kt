package com.example.wordcrushgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)

        val score = intent.getIntExtra("score", 0)

        val tvScore = findViewById<TextView>(R.id.tvFinalScore)
        val btnHome = findViewById<Button>(R.id.btnHome)
        val btnReplay = findViewById<Button>(R.id.btnReplay)

        tvScore.text = "Skor: $score"

        // 🏠 Ana Menü
        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // 🔄 Tekrar Oyna
        btnReplay.setOnClickListener {
            startActivity(Intent(this, GridSelectionActivity::class.java))
            finish()
        }
    }
}