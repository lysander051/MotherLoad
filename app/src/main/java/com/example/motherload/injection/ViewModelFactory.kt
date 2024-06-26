package com.example.motherload.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.motherload.ui.connexion.ConnexionViewModel
import com.example.motherland.MotherLoad
import com.example.motherload.data.Repository
import com.example.motherload.ui.game.home.HomeViewModel
import com.example.motherload.ui.game.inventory.InventoryViewModel
import com.example.motherload.ui.game.profile.ProfileViewModel
import com.example.motherload.ui.game.shop.ShopViewModel


/**
 * @property repository l'instance de Repository
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory private constructor() : ViewModelProvider.Factory{
    private var repository: Repository? = null


    init {
        repository = Repository.getInstance(MotherLoad.instance)
    }

    /**
     * Génère un ViewModel particulier sur demande
     *
     * @param modelClass la classe de ViewModel à générer
     * @return le ViewModel ainsi créé
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnexionViewModel::class.java)) {
            return ConnexionViewModel(repository!!) as T
        }
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository!!) as T
        }
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(repository!!) as T
        }
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            return InventoryViewModel(repository!!) as T
        }
        if (modelClass.isAssignableFrom(ShopViewModel::class.java)) {
            return ShopViewModel(repository!!) as T
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