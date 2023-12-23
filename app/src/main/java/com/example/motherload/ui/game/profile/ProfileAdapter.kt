package com.example.motherload.ui.game.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.motherload.R
import com.example.motherload.data.ItemDescription
import com.squareup.picasso.Picasso

class ProfileAdapter(private val itemList: List<ItemDescription?>) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_artefact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageItem)
        private val nameTextView: TextView = itemView.findViewById(R.id.name)

        fun bind(item: ItemDescription?) {
            if (item?.quantity?.toInt() != 0) {
                Picasso.get().load(item?.image).into(imageView)
                nameTextView.text = item?.nom
            }
        }
    }
}