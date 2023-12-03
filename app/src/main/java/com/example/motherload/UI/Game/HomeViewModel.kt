package com.example.motherload.UI.Game

import android.location.Location
import androidx.lifecycle.ViewModel
import com.example.motherLoad.Data.ConnexionRepo
import com.example.motherload.Data.ConnexionCallback
import com.example.motherload.Data.HomeCallback
import com.example.motherload.Data.HomeRepo

class HomeViewModel(var homeRepo: HomeRepo): ViewModel() {
    fun deplacement(location: Location, callback: HomeCallback){
        val latitude = location.latitude
        val longitude = location.longitude
        homeRepo.deplacement(latitude, longitude, callback)
    }

}