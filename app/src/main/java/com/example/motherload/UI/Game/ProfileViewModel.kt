package com.example.motherload.UI.Game

import androidx.lifecycle.ViewModel
import com.example.motherload.Data.HomeCallback
import com.example.motherload.Data.ProfilCallback
import com.example.motherload.Data.ProfileRepo

class ProfileViewModel(var profileRepo: ProfileRepo): ViewModel() {
    fun changerPseudo(pseudo: String, callback: ProfilCallback){
        profileRepo.changerPseudo(pseudo, callback)
    }
}