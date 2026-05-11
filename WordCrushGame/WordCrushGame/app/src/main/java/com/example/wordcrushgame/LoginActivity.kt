package com.example.wordcrushgame

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("USER_DATA", MODE_PRIVATE)

        val isChangeMode = intent.getBooleanExtra("CHANGE_NAME", false)
        val savedName = prefs.getString("USERNAME", null)

        // 🔥 SADECE ilk girişte otomatik geç
        if (savedName != null && !isChangeMode) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val editName = findViewById<EditText>(R.id.editName)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnBack = findViewById<TextView>(R.id.btnBack) // 🔙 EKLENDİ

        // 🔥 Eğer isim varsa editText'e yaz (değiştirme modunda)
        if (savedName != null) {
            editName.setText(savedName)
        }

        // 🔙 GERİ BUTONU
        btnBack.setOnClickListener {
            finish()
        }

        btnStart.setOnClickListener {
            val name = editName.text.toString().trim()

            if (name.isNotEmpty()) {
                prefs.edit().putString("USERNAME", name).apply()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}