package com.example.wordcrushgame

import android.content.Context
import android.util.Log

class GoldManager(context: Context) {

    private val prefs = context.getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GOLD = "GOLD"
        private const val DEFAULT_GOLD = 100000
    }

    init {
        // 🔥 SADECE İLK AÇILIŞTA ÇALIŞIR
        if (!prefs.contains(KEY_GOLD)) {
            prefs.edit().putInt(KEY_GOLD, DEFAULT_GOLD).apply()
            Log.d("GOLD_DEBUG", "İlk açılış → $DEFAULT_GOLD altın verildi")
        }
    }

    // 🔹 Altını getir
    fun getGold(): Int {
        val value = prefs.getInt(KEY_GOLD, 0)
        Log.d("GOLD_DEBUG", "GET GOLD: $value")
        return value
    }

    // 🔹 Altını set et
    fun setGold(value: Int) {
        prefs.edit().putInt(KEY_GOLD, value).apply()
        Log.d("GOLD_DEBUG", "SET GOLD: $value")
    }

    // 🔹 Altın azalt (satın alma)
    fun decreaseGold(amount: Int): Boolean {
        val current = getGold()

        return if (current >= amount) {
            val newGold = current - amount
            setGold(newGold)
            Log.d("GOLD_DEBUG", "ALTIN AZALTILDI → $newGold")
            true
        } else {
            Log.d("GOLD_DEBUG", "YETERSİZ ALTIN → $current")
            false
        }
    }

    // 🔹 Altın artır (ödül vs.)
    fun increaseGold(amount: Int) {
        val current = getGold()
        val newGold = current + amount
        setGold(newGold)
        Log.d("GOLD_DEBUG", "ALTIN ARTTI → $newGold")
    }

    // 🔹 Debug amaçlı sıfırlama (opsiyonel)
    fun resetGold() {
        setGold(DEFAULT_GOLD)
        Log.d("GOLD_DEBUG", "ALTIN RESETLENDİ → $DEFAULT_GOLD")
    }
}