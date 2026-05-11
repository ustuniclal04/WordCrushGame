package com.example.wordcrushgame

import android.content.Context

class JokerManager(context: Context) {

    private val prefs = context.getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE)

    companion object {
        const val JOKER_FISH = "JOKER_FISH"
        const val JOKER_WHEEL = "JOKER_WHEEL"
        const val JOKER_LOLLIPOP = "JOKER_LOLLIPOP"
        const val JOKER_SWAP = "JOKER_SWAP"
        const val JOKER_SHUFFLE = "JOKER_SHUFFLE"
        const val JOKER_PARTY = "JOKER_PARTY"
    }

    // 🔥 Joker sayısını getir
    fun getJokerCount(key: String): Int {
        return prefs.getInt(key, 0)
    }

    // 🔥 Joker ekle
    fun addJoker(key: String) {
        val current = getJokerCount(key)
        prefs.edit().putInt(key, current + 1).apply()
    }

    // 🔥 Joker azalt (kullanınca)


    // 🔥 Toplam joker sayısı
    fun getTotalJokers(): Int {
        return getJokerCount(JOKER_FISH) +
                getJokerCount(JOKER_WHEEL) +
                getJokerCount(JOKER_LOLLIPOP) +
                getJokerCount(JOKER_SWAP) +
                getJokerCount(JOKER_SHUFFLE) +
                getJokerCount(JOKER_PARTY)
    }

    // 🔥 Tüm jokerleri sıfırla (debug / reset için)
    fun resetAll() {
        prefs.edit().clear().apply()
    }

    fun useJoker(key: String): Boolean {
        val current = getJokerCount(key)

        return if (current > 0) {
            prefs.edit().putInt(key, current - 1).apply()
            true
        } else {
            false
        }
    }
}