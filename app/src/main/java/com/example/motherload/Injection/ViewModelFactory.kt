package com.example.motherLoad.Injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.motherLoad.UI.Connexion.ConnexionViewModel
import com.example.motherload.Data.ProfileRepo
import com.example.motherload.Data.Repository
import com.example.motherload.UI.Game.HomeViewModel
import com.example.motherload.UI.Game.InventoryViewModel
import com.example.motherload.UI.Game.ProfileViewModel


@Suppress("UNCHECKED_CAST")
class ViewModelFactory private constructor() : ViewModelProvider.Factory{
    private var repository: Repository? = null
    private var profileRepo: ProfileRepo? = null


    init {
        repository = Repository()
        profileRepo = ProfileRepo()
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnexionViewModel::class.java)) {
            return ConnexionViewModel(repository!!) as T
        }
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository!!) as T
        }
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(profileRepo!!) as T
        }
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            return InventoryViewModel(repository!!) as T
        }
        throw IllegalArgumentException("La classe viewmodel choisie est inconnue")
    }

    companion object {
        private var factory: ViewModelFactory? = null
        val getInstance: ViewModelFactory?
            get() {
                if (factory == null) {
                    synchronized(ViewModelFactory::class.java) {
                        if (factory == null) {
                            factory = ViewModelFactory()
                        }
                    }
                }
                return factory
            }
    }
}