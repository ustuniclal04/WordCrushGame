package com.example.wordcrushgame

class SpecialManager(
    private val grid: Array<Array<Cell>>,
    private val gridSize: Int,
    private val effectClearCell: (Int, Int) -> Unit
) {

    // Geriye Boolean döndürecek şekilde güncelledik
    fun createSpecialPower(selectedCells: List<Pair<Int, Int>>): Boolean {
        val length = selectedCells.size
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
        return power != null // Eğer güç oluştuysa true, oluşmadıysa false döner
    }

    fun triggerSpecial(row: Int, col: Int) {

        val cell = grid[row][col]

        when (cell.special) {

            // 🔴 SATIR TEMİZLEME
            "SATIR_TEMIZLEME" -> {
                for (c in 0 until gridSize) {
                    effectClearCell(row, c)
                }
            }

            // 🔴 SÜTUN TEMİZLEME
            "SUTUN_TEMIZLEME" -> {
                for (r in 0 until gridSize) {
                    effectClearCell(r, col)
                }
            }

            // 🔴 3x3 PATLATMA
            "ALAN_PATLATMA" -> {
                for (dr in -1..1) {
                    for (dc in -1..1) {

                        val nr = row + dr
                        val nc = col + dc

                        if (nr in 0 until gridSize && nc in 0 until gridSize) {
                            effectClearCell(nr, nc)
                        }
                    }
                }
            }

            // 🔴 5x5 MEGA PATLATMA
            "MEGA_PATLATMA" -> {
                for (dr in -2..2) {
                    for (dc in -2..2) {

                        val nr = row + dr
                        val nc = col + dc

                        if (nr in 0 until gridSize && nc in 0 until gridSize) {
                            effectClearCell(nr, nc)
                        }
                    }
                }
            }
        }

        // 🔥 özel gücü sıfırla
        cell.special = null
    }
}