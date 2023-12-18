package com.example.motherload.ui.game.profile

import androidx.lifecycle.ViewModel
import com.example.motherload.data.ProfilCallback
import com.example.motherload.data.Repository

class ProfileViewModel(var profileRepo: Repository): ViewModel() {
    fun changerPseudo(pseudo: String, callback: ProfilCallback){
        profileRepo.changerPseudo(pseudo, callback)
    }
    fun resetUser(callback: ProfilCallback){
        profileRepo.resetUser(callback)
    }
}