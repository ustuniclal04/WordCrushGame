package com.example.wordcrushgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class GridSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid_selection)

        // 🔙 GERİ BUTONU (ekrandaki buton)
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btn6).setOnClickListener {
            goNext(6)
        }

        findViewById<Button>(R.id.btn8).setOnClickListener {
            goNext(8)
        }

        findViewById<Button>(R.id.btn10).setOnClickListener {
            goNext(10)
        }
    }

    private fun goNext(gridSize: Int) {
        val intent = Intent(this, MoveSelectionActivity::class.java)
        intent.putExtra("GRID_SIZE", gridSize)
        startActivity(intent)
    }
}