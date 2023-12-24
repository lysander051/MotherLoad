package com.example.motherload.data.callback

import com.example.motherload.data.Item

interface ShopCallback {
    /**
     *  Transmet le résultat de la requête market_list
     *
     *  @param items une liste de triple (id de l'offre, l'Item(id,quantité), et le prix)
     */
    fun getMarketItems(items: List<Triple<Int, Item, Int>>)

    /**
     * Transmet le résultat de la requête status_joueur auquel on ne conserve que l'inventaire
     *
     * @param inventory l'inventaire du joueur
     */
    fun getInventory(items: MutableList<Item>)

    /**
     * Transmet le résultat de la requête market_acheter
     */
    fun buyItem()

    /**
     * Transmet le résultat de la requête market_vendre
     */
    fun sellItem()

    /**
     * Transmet une erreur d'une requête
     */
    fun erreur()
}