package com.example.wordcrushgame

import android.R.attr.gravity
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MarketActivity : AppCompatActivity() {

    private lateinit var goldManager: GoldManager
    private lateinit var textGold: TextView
    private lateinit var txtJokerInfo: TextView
    private lateinit var btnBack: TextView // ✅ DÜZELTİLDİ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)

        goldManager = GoldManager(this)

        textGold = findViewById(R.id.textGold)
        txtJokerInfo = findViewById(R.id.txtJokerInfo)
        btnBack = findViewById(R.id.btnBack) // ✅ ARTIK TextView

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerMarket)

        btnBack.setOnClickListener {
            finish()
        }

        updateGold()
        updateJokerInfo()

        val jokerList = listOf(
            Joker("Balık", "Rastgele harfleri yok eder.", 100, R.drawable.ic_fish),
            Joker("Tekerlek", "Satır ve sütunu yok eder.", 200, R.drawable.ic_wheel),
            Joker("Lolipop Kırıcı", "Tek harfi yok eder.", 75, R.drawable.ic_lollipop),
            Joker("Serbest Değiştirme", "İki harfi değiştirir.", 125, R.drawable.ic_swap),
            Joker("Harf Karıştırma", "Gridi karıştırır.", 300, R.drawable.ic_shuffle),
            Joker("Parti Güçlendirici", "Tüm grid resetlenir.", 400, R.drawable.ic_party)
        )

        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = MarketAdapter(jokerList, goldManager) {
            updateGold()
            updateJokerInfo()
        }
    }

    private fun updateGold() {
        textGold.text = "Altın: ${goldManager.getGold()}"
    }

    private fun updateJokerInfo() {
        val jm = JokerManager(this)

        val fish = jm.getJokerCount(JokerManager.JOKER_FISH)
        val wheel = jm.getJokerCount(JokerManager.JOKER_WHEEL)
        val lollipop = jm.getJokerCount(JokerManager.JOKER_LOLLIPOP)
        val swap = jm.getJokerCount(JokerManager.JOKER_SWAP)
        val shuffle = jm.getJokerCount(JokerManager.JOKER_SHUFFLE)
        val party = jm.getJokerCount(JokerManager.JOKER_PARTY)
    }

    override fun onResume() {
        super.onResume()
        updateGold()
        updateJokerInfo()
    }
    fun showJokerVideo(videoResId: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_video)

        // Arka planı şeffaf yap
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val videoView = dialog.findViewById<VideoView>(R.id.videoView)
        val uri = Uri.parse("android.resource://$packageName/$videoResId")
        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener { mp ->
            val videoWidth = mp.videoWidth
            val videoHeight = mp.videoHeight
            val screenHeight = resources.displayMetrics.heightPixels
            val newWidth = (screenHeight * videoWidth) / videoHeight

            videoView.layoutParams = FrameLayout.LayoutParams(newWidth, screenHeight).apply {
                gravity = Gravity.CENTER
            }
            mp.isLooping = true
            videoView.start()
        }

        videoView.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}