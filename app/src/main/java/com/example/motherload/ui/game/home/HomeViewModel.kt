package com.example.motherload.ui.game.home

import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.motherland.MotherLoad
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.data.callback.HomeCallback
import com.example.motherload.data.Repository
import com.example.motherload.data.callback.ItemCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    fun deplacement(location: Location, callback: HomeCallback, activity: Activity){
        val latitude = location.latitude
        val longitude = location.longitude
        homeRepo.deplacement(latitude, longitude, callback, activity)
    }

    fun creuser(location: GeoPoint, callback: HomeCallback, activity: Activity) {
        val latitude = location.latitude
        val longitude = location.longitude
        homeRepo.creuser(latitude, longitude, callback, activity)
    }

    fun getDepthHole(): Triple<Float, Float, Int> {
        return homeRepo.getDepthHole()
    }
    fun getItems(item: List<Item>, callback: ItemCallback, activity: Activity){
        homeRepo.getItems(item, callback, activity)
    }
}