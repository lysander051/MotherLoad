package com.example.motherload.data.callback

import com.example.motherload.data.ItemDescription
import org.osmdroid.util.GeoPoint

interface HomeCallback {
    fun deplacement(voisin: MutableMap<String, GeoPoint>)
    fun creuse(itemId: Int, depht: String, voisin: MutableMap<String, GeoPoint>)
    fun erreur(erreurId: Int)
}