package com.example.motherload.UI.Game

import android.location.Location
import androidx.lifecycle.ViewModel
import com.example.motherload.Data.HomeCallback
import com.example.motherload.Data.HomeRepo
import org.osmdroid.util.GeoPoint

class HomeViewModel(var homeRepo: HomeRepo): ViewModel() {
    fun deplacement(location: Location, callback: HomeCallback){
        val latitude = location.latitude
        val longitude = location.longitude
        homeRepo.deplacement(latitude, longitude, callback)
    }

    fun creuser(location: GeoPoint, callback: HomeCallback) {
        val latitude = location.latitude
        val longitude = location.longitude
        homeRepo.creuser(latitude, longitude, callback)
    }
}