package com.example.motherload.UI.Inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.motherland.view.InventoryFragment
import com.example.motherload.Data.Item
import com.example.motherload.R

class ItemRecyclerViewAdapter(private val iListener: InventoryFragment.OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder>(){

    private val iValues = mutableListOf<Item>()

    // Utilisez cette fonction pour mettre à jour la liste depuis le ViewModel
    fun updateInventory(newItems: List<Item>) {
        iValues.clear()
        iValues.addAll(newItems)
        notifyDataSetChanged()
    }

    // Cette méthode est utilisée par la RecyclerView quand elle a besoin de créer les éléments
    // graphiques composant un item de liste.
    // NB : on n'en créé par un pour chaque élément de la liste : le recyclierView est prévue pour
    // réutiliser ceux qui ne sont plus actuellement afffichés.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    // Cette méthode est utilisée par la reciclerView pour afficher un message dans un item de liste.
    // Il faut donc dans cette méthode mettre à jor les éléments de 'holder' de manière à ce qu'ils
    // affichent le message en position 'position'.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.iItem = iValues[position]
        holder.iIdView.text = iValues[position].id.toString()
        holder.iQuantityView.text = iValues[position].Quantity.toString()
        holder.iView.setOnClickListener { iListener?.onListFragmentInteraction(holder.iItem) }
    }

    // Doit retourner le nombre d'élément à afficher dnas la liste
    override fun getItemCount(): Int {
        return iValues.size
    }

    // Notre viewHolder. Il s'agit de l'élément graphique correspondant à un item de liste.
    // Le notre contient 3 champs texte destinés à afficher la date, l'auteur et le début du contenu
    // du message
    inner class ViewHolder(val iView: View) : RecyclerView.ViewHolder(iView) {
        val iIdView: TextView
        val iQuantityView: TextView
        var iItem: Item? = null

        init {
            iIdView = iView.findViewById<View>(R.id.id) as TextView
            iQuantityView = iView.findViewById<View>(R.id.quantity) as TextView
        }
    }
}