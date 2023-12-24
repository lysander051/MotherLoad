package com.example.motherload.data.callback

import com.example.motherload.data.ItemDescription

interface ItemCallback {
    /**
     * Transmet le résultat de la requête item_detail
     *
     * @param itemDescription la liste des itemDescription correspondant aux items du joueur
     */
    fun getItemsDescription(itemDescription: MutableList<ItemDescription>)
}