package com.example.motherload.ui.game.profile

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.motherload.data.Item
import com.example.motherload.data.callback.ProfilCallback
import com.example.motherload.data.Repository
import com.example.motherload.data.callback.ItemCallback

/**
 * @param profileRepo l'instance de repository
 */
class ProfileViewModel(var profileRepo: Repository): ViewModel() {
    /**
     * Transmet les informations de la requête changer de pseudo au repo
     *
     * @param pseudo le nouveau pseudonyme
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun changerPseudo(pseudo: String, callback: ProfilCallback, activity: Activity){
        profileRepo.changerPseudo(pseudo, callback, activity)
    }

    /**
     * Transmet les information de la requête reset user au repo
     *
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun resetUser(callback: ProfilCallback, activity: Activity){
        profileRepo.resetUser(callback, activity)
    }

    /**
     * Transmet les informations de la requête liste d'artéfact au repo
     *
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun getArtifact(callback: ProfilCallback, activity: Activity){
        profileRepo.getArtifact(callback, activity)
    }

    /**
     * Transmet les informations de la requête get status joueur ou l'on va seulement garder l'inventaire au repo
     *
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun getInventory(callback: ProfilCallback, activity: Activity){
        profileRepo.getInventory(callback, activity)
    }

    /**
     * Transmet les informations de la requête détail item au repo
     *
     * @param item la liste des items dont on veut le détail
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun getItems(item: List<Item>, callback: ItemCallback, activity: Activity){
        profileRepo.getItems(item, callback, activity)
    }
}