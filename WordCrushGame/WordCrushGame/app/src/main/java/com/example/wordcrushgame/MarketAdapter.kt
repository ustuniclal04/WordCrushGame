package com.example.wordcrushgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class MarketAdapter(
    private val list: List<Joker>,
    private val goldManager: GoldManager,
    private val onPurchase: () -> Unit
) : RecyclerView.Adapter<MarketAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.imgIcon)
        val name: TextView = view.findViewById(R.id.textName)
        val desc: TextView = view.findViewById(R.id.textDesc)
        val cost: TextView = view.findViewById(R.id.textCost)
        val btnBuy: Button = view.findViewById(R.id.btnBuy)
        val owned: TextView = view.findViewById(R.id.txtOwned)
        val btnInfo: TextView = view.findViewById(R.id.btnInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_joker, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val joker = list[position]

        holder.icon.setImageResource(joker.icon)
        holder.name.text = joker.name
        holder.desc.text = joker.description
        holder.cost.text = "${joker.cost} 🪙"

        val context = holder.itemView.context
        val jokerManager = JokerManager(context)

        // 1. Joker ID Belirleme (Envanter ve satın alma için)
        val jokerId = when (joker.name) {
            "Balık" -> JokerManager.JOKER_FISH
            "Tekerlek" -> JokerManager.JOKER_WHEEL
            "Lolipop Kırıcı" -> JokerManager.JOKER_LOLLIPOP
            "Serbest Değiştirme" -> JokerManager.JOKER_SWAP
            "Harf Karıştırma" -> JokerManager.JOKER_SHUFFLE
            "Parti Güçlendirici" -> JokerManager.JOKER_PARTY
            else -> ""
        }
        // 2. Video Gösterimi (Bilgi butonu)
        holder.btnInfo.setOnClickListener {
            val videoId = when (joker.name) {
                "Balık" -> R.raw.fish_demo
                "Tekerlek" -> R.raw.wheel_demo
                "Lolipop Kırıcı" -> R.raw.lollipop_demo
                "Serbest Değiştirme" -> R.raw.swap_demo
                "Harf Karıştırma" -> R.raw.shuffle_demo
                "Parti Güçlendirici" -> R.raw.party_demo
                else -> null
            }
            videoId?.let { (context as MarketActivity).showJokerVideo(it) }
        }

        // 🔥 SAHİP OLUNAN SAYI
        val ownedCount = jokerManager.getJokerCount(jokerId)

        if (ownedCount > 0) {
            holder.owned.visibility = View.VISIBLE
            holder.owned.text = ownedCount.toString()
        } else {
            holder.owned.visibility = View.GONE
        }

        holder.btnBuy.setOnClickListener {

            val success = goldManager.decreaseGold(joker.cost)

            if (success) {

                // 🔥 ENVANTERE EKLE
                jokerManager.addJoker(jokerId)

                Toast.makeText(context, "Satın alındı!", Toast.LENGTH_SHORT).show()

                onPurchase()

                // 🔥 ANINDA GÜNCELLE (ÇOK ÖNEMLİ)
                notifyItemChanged(position)

            } else {
                Toast.makeText(context, "Yetersiz altın!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}