package com.example.wordcrushgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MoveSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_selection)

        val gridSize = intent.getIntExtra("GRID_SIZE", 6)

        // 🔙 GERİ BUTONU
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // HAMLE SEÇİMLERİ
        findViewById<Button>(R.id.btnEasy).setOnClickListener {
            startGame(gridSize, 25)
        }

        findViewById<Button>(R.id.btnMedium).setOnClickListener {
            startGame(gridSize, 20)
        }

        findViewById<Button>(R.id.btnHard).setOnClickListener {
            startGame(gridSize, 15)
        }
    }

    private fun startGame(grid: Int, move: Int) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("GRID_SIZE", grid)
        intent.putExtra("MOVE_COUNT", move)
        startActivity(intent)
    }
}