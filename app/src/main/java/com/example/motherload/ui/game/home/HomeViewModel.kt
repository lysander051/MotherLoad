package com.example.motherload.ui.game.home

import android.app.Activity
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.motherload.data.Item
import com.example.motherload.data.callback.HomeCallback
import com.example.motherload.data.Repository
import com.example.motherload.data.callback.ItemCallback
import org.osmdroid.util.GeoPoint

/**
 * @property _isButtonClickEnabled true si le bouton creuser est clicable
 * @property isButtonClickEnabled true si le bouton creuser est clicable
 */
class HomeViewModel(private var homeRepo: Repository): ViewModel() {
    private val _isButtonClickEnabled = MutableLiveData(true)
    val isButtonClickEnabled: LiveData<Boolean> get() = _isButtonClickEnabled

    /**
     * Désactive la possibilité de cliquer sur le bouton creuser
     */
    fun disableButtonClick() {
        _isButtonClickEnabled.value = false
    }

    /**
     * Active la possibilité de cliquer sur le bouton creuser
     */
    fun enableButtonClick() {
        _isButtonClickEnabled.value = true
    }

    /**
     * Envoie les informations nécessaire au repo afin de faire la requête de déplacement
     *
     * @param location la position du joueur
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun deplacement(location: Location, callback: HomeCallback, activity: Activity){
        val latitude = location.latitude
        val longitude = location.longitude
        homeRepo.deplacement(latitude, longitude, callback, activity)
    }

    /**
     * Envoie les informations nécessaire au repo afin de faire la requête creuser
     *
     * @param location la position du joueur
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun creuser(location: GeoPoint, callback: HomeCallback, activity: Activity) {
        val latitude = location.latitude
        val longitude = location.longitude
        homeRepo.creuser(latitude, longitude, callback, activity)
    }

    /**
     * Récupère la profondeur du trou actuel
     */
    fun getDepthHole(): Triple<Float, Float, Int> {
        return homeRepo.getDepthHole()
    }

    /**
     * Envoie les informations nécessaire au repo afin de faire la requête récupérant le détail des items du joueur
     *
     * @param item la liste des items possédé par le joueur
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun getItems(item: List<Item>, callback: ItemCallback, activity: Activity){
        homeRepo.getItems(item, callback, activity)
    }
}