package com.example.wordcrushgame

import android.R.attr.textStyle
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date
import android.content.Intent
import android.widget.ImageView
import android.widget.Toast
import android.app.AlertDialog
import androidx.activity.OnBackPressedCallback
import android.app.Dialog
import android.graphics.Typeface
import android.net.Uri
import android.widget.VideoView
import android.widget.FrameLayout
import androidx.core.content.ContextCompat

import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import android.content.Context

class GameActivity : AppCompatActivity() {

    private var isSaved = false
    private var dictionary = HashSet<String>()
    private lateinit var gridLayout: GridLayout
    private lateinit var tvMoveCount: TextView
    private val selectedTextViews = mutableListOf<TextView>() // Görsel vurgu için
    private var currentSelectionString = "" // Kelime kontrolü için
    private var isSelecting = false // Kullanıcı sürükleme yapıyor mu?
    private var gridSize = 6
    private var moveCount = 20


    private var score = 0
    private var wordCount = 0
    private var longestWord = ""

    private var startTime: Long = 0L

    private var activeJoker: String? = null
    private var firstSelected: TextView? = null

    private lateinit var tvFoundWords: TextView
    private val foundWordsList = mutableListOf<String>()

    private val currentGameWords = mutableListOf<String>()
    // Harf Puan Tablosu
    private val letterPoints = mapOf(
        'A' to 1, 'E' to 1, 'K' to 1, 'L' to 1, 'R' to 1, 'N' to 1, 'T' to 1, 'İ' to 1,
        'I' to 2, 'M' to 2, 'S' to 2, 'O' to 2, 'U' to 2,
        'B' to 3, 'D' to 3, 'Y' to 3, 'Ü' to 3,
        'C' to 4, 'Ç' to 4, 'Ş' to 4, 'Z' to 4,
        'G' to 5, 'H' to 5, 'P' to 5,
        'F' to 7, 'Ö' to 7, 'V' to 7,
        'Ğ' to 8, 'J' to 10
    )

    lateinit var grid: Array<Array<Cell>>
    lateinit var specialManager: SpecialManager

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val jm = JokerManager(this)

        val imgFish = findViewById<ImageView>(R.id.imgFish)
        val imgWheel = findViewById<ImageView>(R.id.imgWheel)
        val imgLollipop = findViewById<ImageView>(R.id.imgLollipop)
        val imgSwap = findViewById<ImageView>(R.id.imgSwap)
        val imgShuffle = findViewById<ImageView>(R.id.imgShuffle)
        val imgParty = findViewById<ImageView>(R.id.imgParty)

        val ids = listOf(
            R.id.btnFishInfo,
            R.id.btnWheelInfo,
            R.id.btnLollipopInfo,
            R.id.btnSwapInfo,
            R.id.btnShuffleInfo,
            R.id.btnPartyInfo
        )

        ids.forEach {
            val btn = findViewById<TextView>(it)

            val shape = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(0x99000000.toInt())
            }

            btn.background = shape
        }


        val txtFish = findViewById<TextView>(R.id.txtFishCount)
        val txtWheel = findViewById<TextView>(R.id.txtWheelCount)
        val txtLollipop = findViewById<TextView>(R.id.txtLollipopCount)
        val txtSwap = findViewById<TextView>(R.id.txtSwapCount)
        val txtShuffle = findViewById<TextView>(R.id.txtShuffleCount)
        val txtParty = findViewById<TextView>(R.id.txtPartyCount)

        // 👇 SAYILARI YAZ
        txtFish.text = jm.getJokerCount(JokerManager.JOKER_FISH).toString()
        txtWheel.text = jm.getJokerCount(JokerManager.JOKER_WHEEL).toString()
        txtLollipop.text = jm.getJokerCount(JokerManager.JOKER_LOLLIPOP).toString()
        txtSwap.text = jm.getJokerCount(JokerManager.JOKER_SWAP).toString()
        txtShuffle.text = jm.getJokerCount(JokerManager.JOKER_SHUFFLE).toString()
        txtParty.text = jm.getJokerCount(JokerManager.JOKER_PARTY).toString()


        val btnBack = findViewById<TextView>(R.id.btnBack)

        btnBack.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Oyundan Çık")
                .setMessage("Çıkmak istediğine emin misin?")
                .setPositiveButton("Evet") { _, _ ->

                    saveScoreAndExit()
                }
                .setNegativeButton("Hayır", null)
                .show()
        }


        tvMoveCount = findViewById(R.id.tvMoveCount)
        val tvScore = findViewById<TextView>(R.id.tvScore)
        tvFoundWords = findViewById(R.id.tvFoundWords)

        if (moveCount <= 0) {
            saveScoreAndExit()
        }

        tvScore.text = "Skor: $score"
        // Hamle sayısı düştüğünde Logcat'te görmek için handleCorrectWord içine ekle:
        Log.d("GAME_DEBUG", "Kalan Hamle Sayısı: $moveCount")

        gridLayout = findViewById(R.id.gridLayout)

        gridSize = intent.getIntExtra("GRID_SIZE", 6)
        moveCount = intent.getIntExtra("MOVE_COUNT", 20)
        tvMoveCount.text = "Hamle: $moveCount"
        gridLayout.rowCount = gridSize
        gridLayout.columnCount = gridSize

        startTime = SystemClock.elapsedRealtime()

        // 2. Bir Yükleme Diyaloğu oluştur
        val loadingDialog = AlertDialog.Builder(this)
            .setMessage("Sözlük ve oyun alanı hazırlanıyor...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        // 3. Sözlüğü Thread içinde yükle
        Thread {
            loadDictionary() // 63 bin kelimeyi yükler

            // 4. Sözlük yüklendikten sonra UI kanalına dön ve Gridi oluştur
            runOnUiThread {
                setupGameGrid() // Gridi oluşturan yeni fonksiyonumuz
                loadingDialog.dismiss() // Bekleme ekranını kapat
            }
        }.start()

        // 🔥 GRID
        gridLayout.post {

            gridLayout.removeAllViews()

            val letters = listOf(
                'A', 'E', 'İ', 'L', 'R', 'N',
                'K', 'M', 'T', 'S', 'Y', 'D',
                'J', 'Ğ', 'F', 'V'
            )

            grid = Array(gridSize) {
                Array(gridSize) {
                    Cell(generateTurkishLetter())
                }
            }
            specialManager = SpecialManager(grid, gridSize) { r, c ->
                effectClearCell(r, c)
            }
            for (i in 0 until gridSize * gridSize) {
                val tv = TextView(this).apply {

                    val row = i / gridSize
                    val col = i % gridSize

                    text = grid[row][col].letter.toString()

                    textSize = 22f
                    gravity = Gravity.CENTER
                    setBackgroundColor(Color.LTGRAY)
                    setTextColor(Color.BLACK)
                    // Kutunun indeksini kaydediyoruz ki sürüklerken bulalım
                    tag = i
                    isClickable = false   // 👈 Tıklanabilir olmasın ki Grid dokunmayı alabilsin
                    isFocusable = false    // 👈 Odaklanmasın
                }


                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    rowSpec = GridLayout.spec(i / gridSize, 1f)
                    columnSpec = GridLayout.spec(i % gridSize, 1f)
                    setMargins(4, 4, 4, 4)
                }

                tv.layoutParams = params
                gridLayout.addView(tv)
            }

            val availableCount = countAvailableWords()
            if (availableCount == 0) {
                // Kelime yoksa harfleri yeniden dağıt (Recursive/Döngüsel olarak)
                setupValidGrid()
            } else {
                updateAvailableWordsUI(availableCount)
            }
        }
        // 🐟 FISH (Rastgele bir harfi patlatır)
        imgFish.setOnClickListener {
            val jm = JokerManager(this)

            // 🛡️ Önce sayı kontrolü
            if (jm.getJokerCount(JokerManager.JOKER_FISH) <= 0) {
                Toast.makeText(this, "Fish jokeriniz kalmadı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!canSelectNewJoker()) return@setOnClickListener

            // Kullanımı onayla ve fonksiyonu çağır
            if (jm.useJoker(JokerManager.JOKER_FISH)) {
                useFish()
                updateJokerUI()
                Toast.makeText(this, "Bir harf uçuruldu!", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔄 SHUFFLE (Anlık çalışanlar için direkt kontrol yeterli)
        imgShuffle.setOnClickListener {
            val jm = JokerManager(this)
            if (jm.getJokerCount(JokerManager.JOKER_SHUFFLE) <= 0) {
                Toast.makeText(this, "Shuffle jokeriniz kalmadı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!canSelectNewJoker()) return@setOnClickListener
            if (jm.useJoker(JokerManager.JOKER_SHUFFLE)) {
                useShuffle()
                updateJokerUI()
            }
        }

        // 🥳 PARTY (Ekrandaki tüm harfleri patlatır)
        imgParty.setOnClickListener {
            val jm = JokerManager(this)

            // 🛡️ Önce sayı kontrolü
            if (jm.getJokerCount(JokerManager.JOKER_PARTY) <= 0) {
                Toast.makeText(this, "Party jokeriniz kalmadı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!canSelectNewJoker()) return@setOnClickListener

            // Kullanımı onayla ve fonksiyonu çağır
            if (jm.useJoker(JokerManager.JOKER_PARTY)) {
                useParty()
                updateJokerUI()
                Toast.makeText(this, "Tüm harfler tazeleniyor!", Toast.LENGTH_SHORT).show()
            }
        }

        // 🍭 LOLLIPOP (Dokun ve Sil)
        imgLollipop.setOnClickListener {
            val jm = JokerManager(this)
            if (jm.getJokerCount(JokerManager.JOKER_LOLLIPOP) <= 0) {
                Toast.makeText(this, "Lollipop jokeriniz kalmadı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (activeJoker == "LOLLIPOP") {
                activeJoker = null
                Toast.makeText(this, "Joker iptal edildi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!canSelectNewJoker()) return@setOnClickListener
            activeJoker = "LOLLIPOP"
            Toast.makeText(
                this,
                "Lollipop Aktif: Silmek istediğiniz harfe dokunun",
                Toast.LENGTH_SHORT
            ).show()
        }

// 🎯 WHEEL (Satır/Sütun Silici)
        imgWheel.setOnClickListener {
            val jm = JokerManager(this)
            if (jm.getJokerCount(JokerManager.JOKER_WHEEL) <= 0) {
                Toast.makeText(this, "Wheel jokeriniz kalmadı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (activeJoker == "WHEEL") {
                activeJoker = null
                Toast.makeText(this, "Joker iptal edildi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!canSelectNewJoker()) return@setOnClickListener
            activeJoker = "WHEEL"
            Toast.makeText(this, "Wheel Aktif: Hedef harfe dokunun", Toast.LENGTH_SHORT).show()
        }

// 🔄 SWAP
        imgSwap.setOnClickListener {

            val jm = JokerManager(this)
            val count = jm.getJokerCount(JokerManager.JOKER_SWAP)

            if (count <= 0) {
                Toast.makeText(this, "Jokeriniz kalmadı!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!canSelectNewJoker()) return@setOnClickListener

            activeJoker = "SWAP"
            firstSelected = null
            Toast.makeText(this, "Serbest Değiştirme Aktif", Toast.LENGTH_SHORT).show()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (activeJoker != null) {
                    activeJoker = null
                    firstSelected = null
                    Toast.makeText(this@GameActivity, "Joker iptal edildi", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    showExitDialog()
                }
            }
        })
        findViewById<TextView>(R.id.btnFishInfo).setOnClickListener {
            showJokerVideo(R.raw.fish_demo)
        }

        findViewById<TextView>(R.id.btnWheelInfo).setOnClickListener {
            showJokerVideo(R.raw.wheel_demo)
        }

        findViewById<TextView>(R.id.btnLollipopInfo).setOnClickListener {
            showJokerVideo(R.raw.lollipop_demo)
        }

        findViewById<TextView>(R.id.btnSwapInfo).setOnClickListener {
            showJokerVideo(R.raw.swap_demo)
        }

        findViewById<TextView>(R.id.btnShuffleInfo).setOnClickListener {
            showJokerVideo(R.raw.shuffle_demo)
        }

        findViewById<TextView>(R.id.btnPartyInfo).setOnClickListener {
            showJokerVideo(R.raw.party_demo)
        }

    }

    fun clearCell(row: Int, col: Int) {

        grid[row][col].letter = ' '
        grid[row][col].special = null

        val index = row * gridSize + col
        val tv = gridLayout.getChildAt(index) as TextView

        tv.text = ""
        tv.setBackgroundColor(Color.LTGRAY)
    }

    fun useParty() {

        val cells = mutableListOf<TextView>()

        // 🔥 tüm hücreleri al
        for (i in 0 until gridLayout.childCount) {
            val tv = gridLayout.getChildAt(i) as TextView
            if (tv.text.isNotEmpty()) {
                cells.add(tv)
            }
        }

        if (cells.isEmpty()) return

        // 🔥 1. AŞAMA → glow
        for (tv in cells) {
            tv.setBackgroundColor(Color.parseColor("#80FFFF00"))
            tv.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(150)
        }

        // 🔥 2. AŞAMA → patlat
        gridLayout.postDelayed({

            for (tv in cells) {
                tv.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(150)
                    .withEndAction {

                        val index = gridLayout.indexOfChild(tv)
                        val row = index / gridSize
                        val col = index % gridSize

                        grid[row][col].letter = ' '
                        grid[row][col].special = null

                        tv.text = ""
                        tv.scaleX = 1f
                        tv.scaleY = 1f
                        tv.setBackgroundColor(Color.LTGRAY)
                    }
            }

            // 🔽 yerçekimi
            gridLayout.postDelayed({
                applyGravity()
                clearSelection()
            }, 200)

        }, 180)
    }

    fun useFish() {

        val candidates = mutableListOf<TextView>()

        for (i in 0 until gridLayout.childCount) {
            val tv = gridLayout.getChildAt(i) as TextView
            if (tv.text.isNotEmpty()) {
                candidates.add(tv)
            }
        }

        if (candidates.isEmpty()) return

        val target = candidates.random()

        // 🔥 Wheel & Lollipop ile AYNI glow
        target.setBackgroundColor(Color.parseColor("#80FFFF00"))

        target.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(120)
            .withEndAction {

                target.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(120)
                    .withEndAction {

                        val index = gridLayout.indexOfChild(target)
                        val row = index / gridSize
                        val col = index % gridSize

                        grid[row][col].letter = ' '
                        grid[row][col].special = null

                        target.text = ""
                        target.scaleX = 1f
                        target.scaleY = 1f
                        target.setBackgroundColor(Color.LTGRAY)

                        applyGravity()
                    }
            }
    }

    fun useShuffle() {

        val centerX = gridLayout.width / 2f
        val centerY = gridLayout.height / 2f

        val cells = mutableListOf<TextView>()
        val letters = mutableListOf<String>()

        // 📦 hücreleri ve harfleri topla
        for (i in 0 until gridLayout.childCount) {
            val tv = gridLayout.getChildAt(i) as TextView
            cells.add(tv)
            letters.add(tv.text.toString())

            // 🔥 sarı glow
            tv.setBackgroundColor(Color.parseColor("#80FFFF00"))
        }

        // 🎯 1. AŞAMA → MERKEZE TOPLAN
        for (tv in cells) {
            val dx = centerX - (tv.x + tv.width / 2)
            val dy = centerY - (tv.y + tv.height / 2)

            tv.animate()
                .translationXBy(dx)
                .translationYBy(dy)
                .setDuration(200)
        }

        // 🎲 2. AŞAMA → SHUFFLE + GERİ DAĞIL
        gridLayout.postDelayed({

            letters.shuffle()

            for (i in cells.indices) {

                cells[i].text = letters[i]
                val row = i / gridSize
                val col = i % gridSize

                grid[row][col].letter = letters[i].first()
            }

            // 💥 geri dağıt
            for (tv in cells) {
                tv.animate()
                    .translationX(0f)
                    .translationY(0f)
                    .setDuration(200)
                    .withEndAction {
                        tv.setBackgroundColor(Color.LTGRAY)
                    }
            }

        }, 220)
    }

    fun updateJokerUI() {
        val jm = JokerManager(this)

        findViewById<TextView>(R.id.txtFishCount).text =
            jm.getJokerCount(JokerManager.JOKER_FISH).toString()

        findViewById<TextView>(R.id.txtWheelCount).text =
            jm.getJokerCount(JokerManager.JOKER_WHEEL).toString()

        findViewById<TextView>(R.id.txtLollipopCount).text =
            jm.getJokerCount(JokerManager.JOKER_LOLLIPOP).toString()

        findViewById<TextView>(R.id.txtSwapCount).text =
            jm.getJokerCount(JokerManager.JOKER_SWAP).toString()

        findViewById<TextView>(R.id.txtShuffleCount).text =
            jm.getJokerCount(JokerManager.JOKER_SHUFFLE).toString()

        findViewById<TextView>(R.id.txtPartyCount).text =
            jm.getJokerCount(JokerManager.JOKER_PARTY).toString()
    }

    private fun canSelectNewJoker(): Boolean {
        if (activeJoker != null) {
            Toast.makeText(this, "Önce mevcut jokeri kullan!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun showExitDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Çıkış")
        builder.setMessage("Oyundan çıkmak istediğinize emin misiniz?")

        // ❌ HAYIR
        builder.setNegativeButton("Hayır") { dialog, _ ->
            dialog.dismiss()
        }

        // ✅ EVET
        builder.setPositiveButton("Evet") { _, _ ->
            saveScoreAndExit()
        }

        builder.setCancelable(false)
        builder.show()
    }

    private fun saveScoreAndExit() {
        saveGameWords()
        Log.d("SAVE_TEST", "BURAYA GIRDI")
        if (isSaved) return // Eğer zaten kaydedildiyse fonksiyonu durdur
        isSaved = true

        val durationSec = ((SystemClock.elapsedRealtime() - startTime) / 1000).toInt()
        val prefs = getSharedPreferences("GAME_DATA", MODE_PRIVATE)
        val gson = Gson()

        val json = prefs.getString("RESULTS", null)
        val type = object : TypeToken<MutableList<GameResult>>() {}.type
        val list: MutableList<GameResult> = if (json.isNullOrEmpty()) mutableListOf() else gson.fromJson(json, type)

        val result = GameResult(
            gameNumber = list.size + 1,
            date = getCurrentDate(),
            gridSize = gridSize,
            score = score,
            wordCount = wordCount,
            longestWord = if (longestWord.isEmpty()) "-" else longestWord,
            duration = durationSec
        )

        list.add(0, result)
        prefs.edit().putString("RESULTS", gson.toJson(list)).apply()

        // Tek bir yerden yönlendirme yap
        val intent = Intent(this, GameResultActivity::class.java)
        intent.putExtra("score", score)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
        Log.d("SAVE_TEST", "KAYIT BITTI")
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private fun selectLetter(tv: TextView) {

        // Eğer hücre patlamışsa veya boşsa seçilmesine izin verme
        if (tv.text.isNullOrEmpty()) return

        if (selectedTextViews.contains(tv)) return // 🛡️ Zaten seçili olan harfi tekrar ekleme!

        val index = tv.tag as Int
        val row = index / gridSize
        val col = index % gridSize

        val cell = grid[row][col]

        tv.setBackgroundColor(Color.YELLOW)
        selectedTextViews.add(tv)

        // 🔥 Harfi eklerken direkt grid'deki güncel harfi al
        currentSelectionString += cell.letter.toString().uppercase(Locale("tr", "TR"))

        Log.d("GAME_DEBUG", "Seçilen harf: ${cell.letter} | Güncel Kelime: $currentSelectionString")
    }

    private fun clearSelection() {
        for (tv in selectedTextViews) {
            tv.setBackgroundColor(Color.LTGRAY)
        }
        selectedTextViews.clear()
        currentSelectionString = ""
    }

    private fun validateWord() {
        val word = currentSelectionString.uppercase(Locale("tr", "TR"))

        // Hiç harf seçilmediyse (dokunup hemen bıraktıysa) işlem yapma, hamle gitmez.
        if (word.isEmpty()) return

        // 🔥 KURAL: Kelime ne olursa olsun, parmağını kaldırdığı an hamle gider!
        moveCount--
        updateUI()

        if (word.length >= 3) {
            if (dictionary.contains(word)) {
                if (!currentGameWords.contains(word)) {
                    currentGameWords.add(word)
                }


                // ✅ KELİME DOĞRU! COMBO HESAPLA
                val (comboTotalScore, comboCount) = calculateComboScore(word)

                score += comboTotalScore
                wordCount++

                // 🔥 BULUNAN KELİMEYİ EKLE
                if (!foundWordsList.contains(word)) {
                    foundWordsList.add(word)
                    updateFoundWordsUI()
                }

                if (word.length > longestWord.length) {
                    longestWord = word
                }

                // Combo bildirimi
                if (comboCount > 1) {
                    Toast.makeText(
                        this,
                        "🔥 $comboCount X COMBO! +$comboTotalScore Puan",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Log.d("GAME_DEBUG", "✅ ONAYLANDI: $word | Combo: $comboCount | Skor: $score")
                handleCorrectWord()
            }
            else {
                // ❌ SÖZLÜKTE YOK (Hamle zaten düştü)
                Log.d("GAME_DEBUG", "❌ SÖZLÜKTE YOK: $word")
            }
        } else {
            // ⚠️ ÇOK KISA (Hamle yine de düştü)
            Log.d("GAME_DEBUG", "⚠️ ÇOK KISA: $word (Hamle harcandı)")
        }

        // Hamle bittiyse oyunu kapat
        if (moveCount <= 0) {
            saveScoreAndExit()
        }
        val testFile = File(filesDir, "game_words.json")
        Log.d("JSON_TEST", testFile.takeIf { it.exists() }?.readText() ?: "Dosya yok")
    }

    private fun calculateComboScore(mainWord: String): Pair<Int, Int> {
        var totalComboScore = 0
        var comboCount = 0
        val foundSubWords = mutableSetOf<String>() // Aynı alt kelimeyi birden fazla saymamak için

        // Alt kelime arama (Örn: MASAL -> MASA, ASA, SAL vb.)
        for (i in 0 until mainWord.length) {
            for (j in i + 3..mainWord.length) { // Minimum 3 harf kuralı
                val subWord = mainWord.substring(i, j)

                if (dictionary.contains(subWord) && !foundSubWords.contains(subWord)) {
                    foundSubWords.add(subWord)
                    comboCount++

                    // Alt kelimenin puanını hesapla ve toplam combo skoruna ekle
                    val subScore = calculateScore(subWord)
                    totalComboScore += subScore

                    Log.d("GAME_DEBUG", "Combo Tespit Edildi: $subWord (+$subScore Puan)")
                }
            }
        }

        // Eğer ana kelime sözlükte olmasına rağmen alt kelime hiç bulunamazsa
        // (ki bu imkansızdır, ana kelimenin kendisi en az 1 combodur),
        // en azından 0 dönmemesini garantiye alıyoruz.
        return Pair(totalComboScore, comboCount)
    }

    // Ekranda güncellemeyi kolaylaştıran yardımcı fonksiyon
    private fun updateUI() {
        findViewById<TextView>(R.id.tvMoveCount).text = "Hamle: $moveCount"
        findViewById<TextView>(R.id.tvScore).text = "Skor: $score"
    }

    private fun handleCorrectWord() {
        val selectedCells = selectedTextViews.map {
            val index = it.tag as Int
            Pair(index / gridSize, index % gridSize)
        }

        val lastRow = selectedCells.last().first
        val lastCol = selectedCells.last().second

        // 1. Mevcut güçleri tetikle
        selectedCells.forEach { (r, c) ->
            if (grid[r][c].special != null) {
                specialManager.triggerSpecial(r, c)
            }
        }

        // 2. Yeni güç üret [cite: 189, 192]
        val hasNewSpecial = specialManager.createSpecialPower(selectedCells)
        refreshGridUI()

        // 3. Harfleri Silme
        for (i in selectedTextViews.indices) {
            val tv = selectedTextViews[i]
            val r = (tv.tag as Int) / gridSize
            val c = (tv.tag as Int) % gridSize

            if (hasNewSpecial && r == lastRow && c == lastCol) {
                // 🔥 KRİTİK: Yeni güç hücresine animasyon uygulama, sadece rengini sıfırla
                tv.animate().cancel() // Varsa devam eden animasyonu durdur
                tv.scaleX = 1f
                tv.scaleY = 1f
                tv.alpha = 1f
                tv.visibility = View.VISIBLE
                continue
            }
            effectClearCell(r, c) // Diğerlerini patlat [cite: 175]
        }

        // 4. Görseli hemen güncelle (Simgeler burada TextView'a yazılır)
        refreshGridUI()
        updateUI()

        if (moveCount <= 0) {
            saveScoreAndExit() // [cite: 236]
            return
        }

        // 5. Gravity ve Kelime Kontrolü
        gridLayout.postDelayed({
            applyGravity() // [cite: 186, 187]
            refreshGridUI() // Harfler düştükten sonra simgeleri tekrar kontrol et

            Thread {
                try {
                    val newCount = countAvailableWords() // [cite: 195, 198]
                    runOnUiThread {
                        if (newCount == 0) {
                            setupValidGrid() // [cite: 203, 207, 211]
                        } else {
                            updateAvailableWordsUI(newCount) // [cite: 199]
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }, 400) // Animasyonların bitmesi için süreyi biraz artırdık
    }

    private fun applyGravity() {

        for (col in 0 until gridSize) {

            val newColumn = mutableListOf<Cell>()

            // 🔽 alttan yukarı doluları topla
            for (row in gridSize - 1 downTo 0) {
                val cell = grid[row][col]
                if (cell.letter != ' ') {
                    newColumn.add(cell)
                }
            }

            // 🔼 yukarıya yeni harf ekle
            while (newColumn.size < gridSize) {
                newColumn.add(Cell(generateTurkishLetter()))
            }

            // 🔄 tekrar grid'e yaz
            for (row in gridSize - 1 downTo 0) {
                grid[row][col] = newColumn[gridSize - 1 - row]
            }
        }

        // 🔥 UI GÜNCELLE
        refreshGridUI()
    }

    private fun refreshGridUI() {
        for (i in 0 until gridLayout.childCount) {
            val tv = gridLayout.getChildAt(i) as TextView
            val row = i / gridSize
            val col = i % gridSize
            val cell = grid[row][col]

            // Hücre boşsa temizle, doluysa özel güce bak
            if (cell.letter == ' ') {
                tv.text = ""
            } else {
                tv.text = when (cell.special) {
                    "SATIR_TEMIZLEME" -> "${cell.letter} ⇆"
                    "SUTUN_TEMIZLEME" -> "${cell.letter} ⇅"
                    "ALAN_PATLATMA" -> "${cell.letter} ✹"
                    "MEGA_PATLATMA" -> "${cell.letter} ★"
                    else -> cell.letter.toString()
                }
            }
            tv.invalidate()
            tv.requestLayout()
            tv.setBackgroundColor(Color.LTGRAY)            // Varsa Tint'i sıfırla (Turuncu kalmaması için)

            tv.scaleX = 1f
            tv.scaleY = 1f
            tv.alpha = 1f
        }
    }


    private fun calculateScore(word: String): Int {
        var total = 0
        for (char in word) {
            total += letterPoints[char] ?: 0
        }
        return total
    }

    private fun findViewAtLocation(x: Float, y: Float): View? {
        // rawX/rawY yerine dokunulan yerin GridLayout içindeki koordinatlarına bakıyoruz
        val rect = Rect()
        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i)
            child.getHitRect(rect) // Hücrenin GridLayout içindeki sınırlarını al
            if (rect.contains(x.toInt(), y.toInt())) {
                return child
            }
        }
        return null
    }

    private fun isNeighbor(lastView: TextView, newView: TextView): Boolean {
        val lastIdx = lastView.tag as Int
        val newIdx = newView.tag as Int

        val lastRow = lastIdx / gridSize
        val lastCol = lastIdx % gridSize
        val newRow = newIdx / gridSize
        val newCol = newIdx % gridSize

        val rowDiff = Math.abs(lastRow - newRow)
        val colDiff = Math.abs(lastCol - newCol)

        // Farklar 1'den küçük veya eşitse komşudur (8 yön)
        return rowDiff <= 1 && colDiff <= 1
    }

    private fun generateTurkishLetter(): Char {
        val random = (1..100).random()
        return when {
            // %70 olasılıkla en sık harfler
            random <= 70 -> "AEİILRN".random()
            // %25 olasılıkla orta sıklıkta harfler
            random <= 95 -> "KMTBHUOSGYD".random()
            // %5 olasılıkla nadir harfler
            else -> "JĞFVÖÜÇZŞCP".random()
        }
    }

    private fun loadDictionary() {
        try {
            assets.open("sozluk.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    // Sadece 3 harf ve üzeri kelimeleri alalım (Döküman kuralı)
                    val word = line.trim().uppercase(Locale("tr", "TR"))
                    if (word.length >= 3) {
                        dictionary.add(word)
                    }
                }
            }
            Log.d("GAME_DEBUG", "Sözlük yüklendi: ${dictionary.size} kelime var.")
        } catch (e: Exception) {
            Log.e("GAME_DEBUG", "Dosya okuma hatası: ${e.message}")
        }
    }

    // Grid üzerindeki toplam oluşturulabilir kelime sayısını döner
    private fun countAvailableWords(): Int {
        val used = BooleanArray(gridSize * gridSize) { false }
        var totalFound = 0

        // Her bir hücreyi başlangıç noktası seçip kelime ara
        for (i in 0 until gridSize * gridSize) {
            // Döküman: "Kelimeler ortak harf kullanamaz" (Madde 8)
            if (!used[i]) {
                val foundWord = findAnyWordFromIndex(i, "", used.copyOf())
                if (foundWord != null) {
                    totalFound++
                    // Bulunan kelimenin harflerini işaretle (Basitlik için bu örnekte
                    // sadece bulunanların sayısını dökümana göre artırıyoruz)
                }
            }
        }
        return totalFound
    }

    private fun findAnyWordFromIndex(idx: Int, current: String, tempUsed: BooleanArray): String? {
        if (tempUsed[idx]) return null

        val tv = gridLayout.getChildAt(idx) as TextView
        val newWord = current + tv.text.toString()

        // Kelimeyi bulduk mu?
        if (newWord.length >= 3 && dictionary.contains(newWord)) {
            return newWord
        }

        // Çok derine inmeyelim (Performans ve mantıklı kelime uzunluğu için)
        if (newWord.length >= 6) return null

        tempUsed[idx] = true

        val row = idx / gridSize
        val col = idx % gridSize

        // 8 yönü tara
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = row + dr
                val nc = col + dc

                if (nr in 0 until gridSize && nc in 0 until gridSize) {
                    val nextIdx = nr * gridSize + nc
                    val res = findAnyWordFromIndex(nextIdx, newWord, tempUsed)
                    if (res != null) return res
                }
            }
        }
        return null
    }

    private fun setupValidGrid() {
        var attempts = 0
        var count = countAvailableWords()

        // 1. Önce birkaç kez dökümandaki Türkçe frekansıyla rastgele deniyoruz
        while (count == 0 && attempts < 5) {
            for (i in 0 until gridLayout.childCount) {
                val tv = gridLayout.getChildAt(i) as TextView
                tv.text = generateTurkishLetter().toString()
            }
            count = countAvailableWords()
            attempts++
        }

        // 2. Eğer hala kelime oluşmadıysa "Kurallı Harf Üretimi" (Madde 4) devreye girer
        if (count == 0) {
            forceInjectWord() // Gride garantili bir kelime yerleştir
            count = countAvailableWords()
        }

        updateAvailableWordsUI(count)
    }

    private fun forceInjectWord() {
        // Sözlükten rastgele 3-4 harfli bir kelime seç (Örn: "ELMA")
        val safeWords = dictionary.filter { it.length in 3..4 }.toList()
        if (safeWords.isEmpty()) return

        val targetWord = safeWords.random()

        // Gridi temizle ve harf frekansıyla doldur
        for (i in 0 until gridLayout.childCount) {
            val tv = gridLayout.getChildAt(i) as TextView
            tv.text = generateTurkishLetter().toString()
        }

        // Kelimeyi rastgele bir satıra yatay olarak yerleştir (Basit ama garantili bir yöntem)
        val randomRow = (0 until gridSize).random()
        val startCol = (0..gridSize - targetWord.length).random()

        for (i in targetWord.indices) {
            val index = (randomRow * gridSize) + (startCol + i)
            val tv = gridLayout.getChildAt(index) as TextView
            tv.text = targetWord[i].toString()
        }
        Log.d("GAME_DEBUG", "Kurallı üretimle yerleştirilen kelime: $targetWord")
    }

    private fun updateAvailableWordsUI(count: Int) {
        findViewById<TextView>(R.id.tvAvailableWords).text =
            "Gridde Oluşturulabilir Kelime Sayısı: $count"
    }

    fun effectClearCell(row: Int, col: Int) {

        val index = row * gridSize + col
        val tv = gridLayout.getChildAt(index) as TextView

        // 🔥 1. DATA TEMİZLE
        grid[row][col].letter = ' '
        grid[row][col].special = null

        // 🔥 2. TEXT'İ HEMEN SİL (EN KRİTİK)
        tv.text = ""

        // 🌟 3. ANİMASYON
        tv.setBackgroundColor(Color.parseColor("#80FFFF00"))

        tv.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(120)
            .withEndAction {

                tv.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction {

                        tv.scaleX = 1f
                        tv.scaleY = 1f
                        tv.alpha = 1f
                        tv.setBackgroundColor(Color.LTGRAY)
                    }
            }
    }

    private fun createSpecialPower(selectedCells: List<Pair<Int, Int>>): Boolean {
        val length = selectedCells.size
        if (length < 4) return false // 3 harfse özel güç oluşmaz

        val (row, col) = selectedCells.last()
        val cell = grid[row][col]

        val power = when {
            length >= 7 -> "MEGA_PATLATMA"
            length == 6 -> "SUTUN_TEMIZLEME"
            length == 5 -> "ALAN_PATLATMA"
            length == 4 -> "SATIR_TEMIZLEME"
            else -> null
        }

        cell.special = power
        return power != null
    }

    private fun performSwap(first: TextView, second: TextView) {
        val index1 = gridLayout.indexOfChild(first)
        val index2 = gridLayout.indexOfChild(second)

        val r1 = index1 / gridSize
        val c1 = index1 % gridSize
        val r2 = index2 / gridSize
        val c2 = index2 % gridSize

        // 1. Grid Matrisindeki (Veri) Değişim
        val tempCell = grid[r1][c1]
        grid[r1][c1] = grid[r2][c2]
        grid[r2][c2] = tempCell

        // 2. Görsel Animasyon ve Yer Değiştirme
        first.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction {
            first.animate().scaleX(1f).scaleY(1f).setDuration(100)
        }
        second.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction {
            second.animate().scaleX(1f).scaleY(1f).setDuration(100)

            // Görsel metinleri güncelle (Simgeler dahil)
            refreshGridUI()
        }
    }

    private fun updateFoundWordsUI() {
        tvFoundWords.text = foundWordsList.joinToString("   |   ") {
            if (it == foundWordsList.last()) "👉 $it" else it
        }
    }

    private fun showJokerVideo(videoResId: Int) {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_video)

        // 🔥 arka planı kaldır
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val videoView = dialog.findViewById<VideoView>(R.id.videoView)

        val uri = Uri.parse("android.resource://$packageName/$videoResId")
        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener { mp ->

            val videoWidth = mp.videoWidth
            val videoHeight = mp.videoHeight

            val screenHeight = resources.displayMetrics.heightPixels

            // 🔥 BOŞLUK SORUNUNU ÇÖZER
            val newWidth = (screenHeight * videoWidth) / videoHeight

            videoView.layoutParams = FrameLayout.LayoutParams(
                newWidth,
                screenHeight
            ).apply {
                gravity = Gravity.CENTER
            }

            mp.isLooping = true
            videoView.start()
        }

        dialog.show()

        videoView.setOnClickListener {
            dialog.dismiss()
        }
    }
    private fun setupGameGrid() {
        gridLayout = findViewById(R.id.gridLayout)
        gridSize = intent.getIntExtra("GRID_SIZE", 6)
        moveCount = intent.getIntExtra("MOVE_COUNT", 20)
        tvMoveCount.text = "Hamle: $moveCount"
        gridLayout.rowCount = gridSize
        gridLayout.columnCount = gridSize

        gridLayout.post {
            gridLayout.removeAllViews()

            // Veri yapısını oluştur
            grid = Array(gridSize) {
                Array(gridSize) {
                    Cell(generateTurkishLetter())
                }
            }

            specialManager = SpecialManager(grid, gridSize) { r, c ->
                effectClearCell(r, c)
            }

            // TextView'ları ekle
            for (i in 0 until gridSize * gridSize) {
                val tv = TextView(this).apply {
                    val row = i / gridSize
                    val col = i % gridSize
                    text = grid[row][col].letter.toString()
                    textSize = 22f
                    gravity = Gravity.CENTER
                    setBackgroundColor(Color.LTGRAY)
                    tag = i
                    isClickable = false
                    isFocusable = false
                }

                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    rowSpec = GridLayout.spec(i / gridSize, 1f)
                    columnSpec = GridLayout.spec(i % gridSize, 1f)
                    setMargins(4, 4, 4, 4)
                }
                tv.layoutParams = params
                gridLayout.addView(tv)
            }

            // Dokunma dinleyicisini kur
            setupGridTouchListener()

            // Sözlük artık dolu olduğu için bu kontrol hatasız çalışır
            val availableCount = countAvailableWords()
            if (availableCount == 0) {
                setupValidGrid()
            } else {
                updateAvailableWordsUI(availableCount)
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupGridTouchListener() {
        gridLayout.setOnTouchListener { v, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    val hitView = findViewAtLocation(event.x, event.y)

                    if (activeJoker != null && hitView is TextView) {

                        val jm = JokerManager(this)

                        when (activeJoker) {

                            // 🍭 LOLLIPOP
                            "LOLLIPOP" -> {

                                if (!jm.useJoker(JokerManager.JOKER_LOLLIPOP)) {
                                    Toast.makeText(this, "Joker yok!", Toast.LENGTH_SHORT)
                                        .show()
                                    activeJoker = null
                                    return@setOnTouchListener true
                                }

                                updateJokerUI()

                                hitView.setBackgroundColor(Color.parseColor("#80FFFF00"))

                                hitView.animate()
                                    .scaleX(1.3f)
                                    .scaleY(1.3f)
                                    .setDuration(120)
                                    .withEndAction {

                                        hitView.animate()
                                            .scaleX(0f)
                                            .scaleY(0f)
                                            .setDuration(120)
                                            .withEndAction {

                                                hitView.text = ""

                                                val index = gridLayout.indexOfChild(hitView)
                                                val row = index / gridSize
                                                val col = index % gridSize

                                                grid[row][col].letter = ' '
                                                grid[row][col].special = null

                                                hitView.scaleX = 1f
                                                hitView.scaleY = 1f
                                                hitView.setBackgroundColor(Color.LTGRAY)

                                                applyGravity()
                                                clearSelection()
                                            }
                                    }


                            }

                            // 🎯 WHEEL
                            "WHEEL" -> {

                                if (!jm.useJoker(JokerManager.JOKER_WHEEL)) {
                                    Toast.makeText(this, "Joker yok!", Toast.LENGTH_SHORT)
                                        .show()
                                    activeJoker = null
                                    return@setOnTouchListener true
                                }

                                updateJokerUI()

                                val index = gridLayout.indexOfChild(hitView)
                                val row = index / gridSize
                                val col = index % gridSize

                                val targets = mutableListOf<TextView>()

                                for (i in 0 until gridLayout.childCount) {
                                    val tv = gridLayout.getChildAt(i) as TextView
                                    val r = i / gridSize
                                    val c = i % gridSize

                                    if (r == row || c == col) {
                                        targets.add(tv)
                                    }
                                }

                                // 🔥 animasyon
                                for (t in targets) {

                                    t.setBackgroundColor(Color.parseColor("#80FFFF00"))

                                    t.animate()
                                        .scaleX(1.3f)
                                        .scaleY(1.3f)
                                        .setDuration(120)
                                        .withEndAction {

                                            t.animate()
                                                .scaleX(0f)
                                                .scaleY(0f)
                                                .setDuration(120)
                                                .withEndAction {

                                                    val index = gridLayout.indexOfChild(t)
                                                    val r = index / gridSize
                                                    val c = index % gridSize

                                                    grid[r][c].letter = ' '
                                                    grid[r][c].special = null

                                                    t.text = ""
                                                    t.scaleX = 1f
                                                    t.scaleY = 1f
                                                    t.setBackgroundColor(Color.LTGRAY)
                                                }
                                        }
                                }

                                // 🔽 gecikmeli gravity
                                gridLayout.postDelayed({
                                    applyGravity()
                                    clearSelection()
                                }, 350)
                            }

                            // 🔄 SWAP - Serbest Değiştirme
                            "SWAP" -> {
                                if (firstSelected == null) {
                                    // Birinci harf seçimi
                                    firstSelected = hitView
                                    hitView.setBackgroundColor(Color.parseColor("#80FFFF00")) // Belirgin bir renk
                                    Toast.makeText(
                                        this,
                                        "Değiştirmek için komşu harfe dokunun",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@setOnTouchListener true
                                } else {
                                    // İkinci harf seçimi
                                    val first = firstSelected!!
                                    val second = hitView

                                    if (first == second) { // Aynı harfe basıldıysa iptal et
                                        first.setBackgroundColor(Color.LTGRAY)
                                        firstSelected = null
                                        activeJoker = null
                                        return@setOnTouchListener true
                                    }

                                    if (isNeighbor(first, second)) {
                                        // Komşularsa joker kullanımını onayla
                                        if (jm.useJoker(JokerManager.JOKER_SWAP)) {
                                            updateJokerUI()

                                            val index1 = gridLayout.indexOfChild(first)
                                            val index2 = gridLayout.indexOfChild(second)
                                            val r1 = index1 / gridSize;
                                            val c1 = index1 % gridSize
                                            val r2 = index2 / gridSize;
                                            val c2 = index2 % gridSize

                                            val glowColor = Color.parseColor("#80FFFF00")

                                            // 🔥 İKİSİNİ DE SARI YAP
                                            first.setBackgroundColor(glowColor)
                                            second.setBackgroundColor(glowColor)

                                            // 🔥 İKİSİNE DE ANİMASYON VER
                                            first.animate()
                                                .scaleX(1.2f)
                                                .scaleY(1.2f)
                                                .setDuration(100)

                                            second.animate()
                                                .scaleX(1.2f)
                                                .scaleY(1.2f)
                                                .setDuration(100)
                                                .withEndAction {

                                                    // 🔁 DATA SWAP
                                                    val tempCell = grid[r1][c1]
                                                    grid[r1][c1] = grid[r2][c2]
                                                    grid[r2][c2] = tempCell

                                                    // 🔄 UI REFRESH
                                                    refreshGridUI()

                                                    first.scaleX = 1f
                                                    first.scaleY = 1f
                                                    second.scaleX = 1f
                                                    second.scaleY = 1f

                                                    firstSelected = null
                                                    activeJoker = null
                                                }
                                        }
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Sadece komşu harfler!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        firstSelected?.setBackgroundColor(Color.LTGRAY)
                                        firstSelected = null
                                        activeJoker = null
                                    }
                                }
                            }
                        }

                        activeJoker = null
                        return@setOnTouchListener true
                    }
                    isSelecting = true
                    v.parent.requestDisallowInterceptTouchEvent(true)


                    if (hitView is TextView) {
                        clearSelection()
                        selectLetter(hitView)
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    // 🛡️ JOKER AKTİFKEN HAREKETİ ENGELLE
                    if (activeJoker != null) return@setOnTouchListener true

                    val hitView = findViewAtLocation(event.x, event.y)

                    if (hitView is TextView) {

                        val lastTv = selectedTextViews.lastOrNull()

                        // 🔁 GERİ GİTME (parmağı geri çekince)
                        if (selectedTextViews.size >= 2 &&
                            hitView == selectedTextViews[selectedTextViews.size - 2]
                        ) {

                            val removed = selectedTextViews.removeLast()
                            removed.setBackgroundColor(Color.LTGRAY)

                            currentSelectionString =
                                currentSelectionString.dropLast(1)

                            return@setOnTouchListener true
                        }

                        // ➕ YENİ HARF EKLE
                        if (!selectedTextViews.contains(hitView) &&
                            (lastTv == null || isNeighbor(lastTv, hitView))
                        ) {

                            selectLetter(hitView)
                        }
                    }

                    true
                }

                MotionEvent.ACTION_UP -> {
                    // 🛡️ JOKER AKTİFKEN UP OLAYINI ENGELLE (Yoksa validateWord çalışır)
                    if (activeJoker != null) return@setOnTouchListener true
                    isSelecting = false
                    validateWord()
                    clearSelection()
                    true
                }

                else -> false
            }
        }
    }

    fun saveGameWords() {
        val file = File(filesDir, "game_words.json")

        val gamesArray = JSONArray()

        // 🔥 ESKİ VERİYİ OKU
        if (file.exists() && file.readText().isNotEmpty()) {
            val json = file.readText()
            val jsonObject = JSONObject(json)

            if (jsonObject.has("games")) {
                val oldGames = jsonObject.getJSONArray("games")

                for (i in 0 until oldGames.length()) {
                    gamesArray.put(oldGames.getJSONObject(i))
                }
            }
        }
        // 🔥 YENİ OYUN
        val gameObject = JSONObject()

        val wordsArray = JSONArray()
        currentGameWords.forEach {
            wordsArray.put(it)
        }

        gameObject.put("words", wordsArray)

        // 🔥 ALT ALTA EKLER
        gamesArray.put(gameObject)

        val finalObject = JSONObject()
        finalObject.put("games", gamesArray)

        file.writeText(finalObject.toString())
    }
}