package com.example.wordcrushgame

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var txtUser: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main_menu)

        // 🔥 TEST ALTIN (istersen sonra kaldır)
        val goldManager = GoldManager(this)
        goldManager.setGold(1000000)

        prefs = getSharedPreferences("USER_DATA", MODE_PRIVATE)

        txtUser = findViewById(R.id.txtUser)
        val btnEdit = findViewById<ImageView>(R.id.btnEditName) // ✏️ EKLENDİ
        val btnNewGame = findViewById<Button>(R.id.btnNewGame)
        val btnScore = findViewById<Button>(R.id.btnScore)
        val btnMarket = findViewById<Button>(R.id.btnMarket)

        // 👤 Kullanıcı adı yükle
        loadUsername()

        // 🔹 Yeni Oyun
        btnNewGame.setOnClickListener {
            startActivity(Intent(this, GridSelectionActivity::class.java))
        }

        // 🔹 Skor
        btnScore.setOnClickListener {
            startActivity(Intent(this, ScoreActivity::class.java))
        }

        // 🔹 Market
        btnMarket.setOnClickListener {
            startActivity(Intent(this, MarketActivity::class.java))
        }

        // ✏️ SADECE KALEM İLE İSİM DEĞİŞTİR
        btnEdit.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("CHANGE_NAME", true)
            startActivity(intent)
        }

        // ❌ ARTIK BUNA GEREK YOK (SİLDİK)
        // txtUser.setOnClickListener { ... }
    }

    // 👤 Kullanıcı adı yükle
    private fun loadUsername() {
        val username = prefs.getString("USERNAME", "Oyuncu")
        txtUser.text = username
    }

    // 🔄 Login'den dönünce isim güncelle
    override fun onResume() {
        super.onResume()
        loadUsername()
    }
}