package com.example.motherLoad.Injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.motherLoad.Data.ConnexionRepo
import com.example.motherLoad.UI.Connexion.ConnexionViewModel
import com.example.motherload.Data.HomeRepo
import com.example.motherload.UI.Game.HomeViewModel


@Suppress("UNCHECKED_CAST")
class ViewModelFactory private constructor() : ViewModelProvider.Factory{

    private var connexionRepo: ConnexionRepo? = null
    private var homeRepo: HomeRepo? = null


    init {
        connexionRepo = ConnexionRepo()
        homeRepo = HomeRepo()
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnexionViewModel::class.java)) {
            return ConnexionViewModel(connexionRepo!!) as T
        }
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(homeRepo!!) as T
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