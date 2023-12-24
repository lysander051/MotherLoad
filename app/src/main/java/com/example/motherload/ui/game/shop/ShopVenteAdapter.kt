package com.example.motherload.ui.game.shop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.motherload.R
import com.example.motherload.data.ItemDescription
import com.squareup.picasso.Picasso
import android.widget.Spinner
import com.example.motherload.utils.setSafeOnClickListener

/**
 * @param itemList la liste des items du joueur
 * @param itemClickListener
 */
class ShopVenteAdapter(private val itemList: List<ItemDescription?>, private val itemClickListener: ShopItemClickListener) : RecyclerView.Adapter<ShopVenteAdapter.ViewHolder>() {
    interface ShopItemClickListener {
        fun onSellButtonClick(quantity: Int, item: ItemDescription?, prix: Int)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.fragment_item_vente, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
        holder.sellButton.setSafeOnClickListener {
            val quantity = holder.quantitySpinner.selectedItem.toString().toInt()
            val price = holder.price.text.toString().toIntOrNull() ?: 0
            itemClickListener.onSellButtonClick(quantity, item, price)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageItem)
        private val nameTextView: TextView = itemView.findViewById(R.id.name)
        val quantitySpinner: Spinner = itemView.findViewById(R.id.quantity)
        val price: EditText = itemView.findViewById(R.id.prix)
        val sellButton: ImageView = itemView.findViewById(R.id.buttonVendre)

        /**
         * Définit l'affichage de l'onglet vente du shop
         */
        fun bind(item: ItemDescription?) {
            Picasso.get().load(item?.image).into(imageView)
            nameTextView.text = item?.nom
            val data = (1..item!!.quantity.toInt()).map { it.toString() }
            val adapter = ArrayAdapter(itemView.context, android.R.layout.simple_spinner_item, data)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            quantitySpinner.adapter = adapter
            quantitySpinner.setSelection(0)
        }
    }
}