package com.example.motherload.data.callback

import com.example.motherload.data.Item

interface InventoryCallback {
    /**
     * Transmet le résultat de la requête status_joueur
     *
     * @param pickaxe la pioche du joueur
     * @param money l'argent du joueur
     * @param inventory la liste des objets du joueur
     */
    fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>)

    /**
     * Transmet le résultat de la requête maj_pioche
     */
    fun upgradePickaxe()

    /**
     * Transmet le résultat de la requête recettes_pioches
     *
     * @param recipe la liste des recette sous forme pioche -> recette
     */
    fun recipePickaxe(recipe: MutableMap<String, List<Item>>)

    /**
     * Transmet une erreur d'une requête
     *
     * @param erreurId l'id de l'erreur rencontrée
     */
    fun erreur(erreurId: Int)
}