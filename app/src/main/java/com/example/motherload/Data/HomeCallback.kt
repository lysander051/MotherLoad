package com.example.motherload.Data

import android.location.Location
import org.osmdroid.util.GeoPoint

interface HomeCallback {
    fun deplacement(voisin: MutableMap<String, GeoPoint>)
    fun creuse(itemId: Int)
    fun erreur(erreurId: Int)
}