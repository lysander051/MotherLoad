package com.example.motherload.ui.game.shop

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.motherload.R
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.ui.game.inventory.InventoryAdapter
import com.example.motherload.utils.setSafeOnClickListener
import com.squareup.picasso.Picasso

class ShopAchatAdapter(private val itemList: List<Triple<Int, ItemDescription?,Int>>, private val itemClickListener: ShopItemClickListener) : RecyclerView.Adapter<ShopAchatAdapter.ViewHolder>() {

    interface ShopItemClickListener {
        fun onBuyButtonClick(order_id: Int, item: ItemDescription?, prix: Int)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item_achat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
        holder.buyButton.setSafeOnClickListener {
            itemClickListener.onBuyButtonClick(item.first, item.second, item.third)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemId(position: Int): Long {
        return itemList[position].first.toLong()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageItem)
        private val nameTextView: TextView = itemView.findViewById(R.id.name)
        private val quantityTextView: TextView = itemView.findViewById(R.id.quantity)
        private val prixTextView: TextView = itemView.findViewById(R.id.prix)
        val buyButton: ImageView = itemView.findViewById(R.id.buttonAcheter)

        fun bind(item: Triple<Int, ItemDescription?, Int>) {
            Picasso.get().load(item.second?.image).into(imageView)
            nameTextView.text = item.second?.nom
            quantityTextView.text = item.second?.quantity
            prixTextView.text = item.third.toString()
        }
    }
}