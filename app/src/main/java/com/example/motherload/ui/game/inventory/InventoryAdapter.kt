package com.example.motherload.ui.game.inventory

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.motherload.data.ItemDescription
import com.example.motherload.R
import com.squareup.picasso.Picasso

class InventoryAdapter(private val itemList: List<ItemDescription?>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    //Permet de réagir au clic d'un utilisateur sur un item de la liste
    interface ItemClickListener {
        fun onItemClick(item: ItemDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view, itemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    /**
     * @param itemView la vue de l'objet
     * @param itemClickListener le listener des objets
     * @property imageView zone de l'image de l'objet
     * @property nameTextView zone de texte du nom de l'objet
     * @property rarityTextView zone de texte de la rareté de l'objet
     * @property quantityTextView zone de texte de la quantité de l'objet
     */
    class ViewHolder(itemView: View, private val itemClickListener: ItemClickListener) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageItem)
        private val nameTextView: TextView = itemView.findViewById(R.id.name)
        private val rarityTextView: TextView = itemView.findViewById(R.id.rarity)
        private val quantityTextView: TextView = itemView.findViewById(R.id.quantity)

        /**
         * Défini les champs texte d'un objet et son listener
         *
         * @param item l'item défini
         */
        fun bind(item: ItemDescription?) {
            Log.d("item", item!!.image)
            Picasso.get().load(item.image).into(imageView)
            nameTextView.text = item.nom
            rarityTextView.text = item.rarity
            quantityTextView.text = item.quantity

            itemView.setOnClickListener {
                itemClickListener.onItemClick(item)
            }
        }
    }
}