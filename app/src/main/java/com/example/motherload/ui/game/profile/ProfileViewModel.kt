package com.example.motherload.ui.game.profile

import androidx.lifecycle.ViewModel
import com.example.motherload.data.Item
import com.example.motherload.data.callback.ProfilCallback
import com.example.motherload.data.Repository
import com.example.motherload.data.callback.InventoryCallback

class ProfileViewModel(var profileRepo: Repository): ViewModel() {
    fun changerPseudo(pseudo: String, callback: ProfilCallback){
        profileRepo.changerPseudo(pseudo, callback)
    }
    fun resetUser(callback: ProfilCallback){
        profileRepo.resetUser(callback)
    }
    fun getArtifact(callback: ProfilCallback){
        profileRepo.getArtifact(callback)
    }
    fun getItems(item: List<Item>, callback: ProfilCallback){
        profileRepo.getItems(item, callback)
    }
}