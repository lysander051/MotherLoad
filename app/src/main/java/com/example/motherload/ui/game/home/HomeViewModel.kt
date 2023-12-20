package com.example.motherload.ui.game.home

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.motherload.data.callback.HomeCallback
import com.example.motherload.data.Repository
import org.osmdroid.util.GeoPoint

class HomeViewModel(var homeRepo: Repository): ViewModel() {
    private val _isButtonClickEnabled = MutableLiveData(true)
    val isButtonClickEnabled: LiveData<Boolean> get() = _isButtonClickEnabled

    fun disableButtonClick() {
        _isButtonClickEnabled.value = false
    }

    fun enableButtonClick() {
        _isButtonClickEnabled.value = true
    }
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