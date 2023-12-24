package com.example.motherload.data.callback

import org.osmdroid.util.GeoPoint

interface HomeCallback {
    /**
     * Transmet le résultat de la requête de deplace
     *
     * @param voisin la liste des voisins sous forme nom -> position
     */
    fun deplacement(voisin: MutableMap<String, GeoPoint>)

    /**
     * Transmet le résultat de la requête creuse
     *
     * @param itemId l'id de l'item trouvé
     * @param depth la profondeur du trou
     * @param voisin la liste des voisins sous forme nom -> position
     */
    fun creuse(itemId: Int, depth: String, voisin: MutableMap<String, GeoPoint>)

    /**
     * Transmet une erreur d'une requête
     *
     * @param erreurId l'id de l'erreur rencontrée
     */
    fun erreur(erreurId: Int)
}