package com.example.wordcrushgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScoreAdapter(private val list: List<GameResult>) :
    RecyclerView.Adapter<ScoreAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvGameNumber: TextView = view.findViewById(R.id.tvGameNumber) // 🔥 EKLENDİ
        val tvScore: TextView = view.findViewById(R.id.tvScore)
        val tvInfo: TextView = view.findViewById(R.id.tvInfo)
        val tvLongest: TextView = view.findViewById(R.id.tvLongest)       // 🔥 EKLENDİ
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // 🔥 OYUN NUMARASI
        holder.tvGameNumber.text = "Oyun #${item.gameNumber}"

        // 🎯 PUAN
        holder.tvScore.text = "Puan: ${item.score}"

        // 🎯 DETAY
        holder.tvInfo.text =
            "${item.gridSize}x${item.gridSize} | ${item.wordCount} kelime | ${item.duration} sn"

        // 🔥 EN UZUN KELİME
        holder.tvLongest.text = "En uzun: ${item.longestWord}"

        // 🎯 TARİH
        holder.tvDate.text = item.date
    }
}