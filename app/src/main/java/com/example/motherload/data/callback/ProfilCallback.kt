package com.example.motherload.data.callback

import com.example.motherload.data.Item

interface ProfilCallback {
    /**
     * Transmet le résultat de la requête changenom
     *
     * @param pseudo le nouveau pseudonyme du joueur
     */
    fun changerPseudo(pseudo: String)

    /**
     * Transmet le résultat de la requête reini_joueur
     */
    fun resetUser()

    /**
     * Transmet le résultat de la requête artefacts_list
     *
     * @param artifact la liste des artéfacts
     */
    fun getArtifact(artifact: List<Item>)

    /**
     * Transmet le résultat de la requête status_joueur auquel on ne conserve que l'inventaire
     *
     * @param inventory l'inventaire du joueur
     */
    fun getInventory(inventory: List<Item>)
}