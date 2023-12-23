package com.example.motherload.ui.game.profile

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.motherload.data.Item
import com.example.motherload.data.callback.ProfilCallback
import com.example.motherload.data.Repository
import com.example.motherload.data.callback.InventoryCallback
import com.example.motherload.data.callback.ItemCallback

class ProfileViewModel(var profileRepo: Repository): ViewModel() {
    @RequiresApi(Build.VERSION_CODES.O)
    fun changerPseudo(pseudo: String, callback: ProfilCallback, activity: Activity){
        profileRepo.changerPseudo(pseudo, callback, activity)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun resetUser(callback: ProfilCallback, activity: Activity){
        profileRepo.resetUser(callback, activity)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getArtifact(callback: ProfilCallback, activity: Activity){
        profileRepo.getArtifact(callback, activity)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getInventory(callback: ProfilCallback, activity: Activity){
        profileRepo.getInventory(callback, activity)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getItems(item: List<Item>, callback: ItemCallback, activity: Activity){
        profileRepo.getItems(item, callback, activity)
    }
}