package com.example.motherload.data

import android.content.Context
import com.example.motherland.MotherLoad
import com.example.motherload.data.api.ConnexionApi
import com.example.motherload.data.api.HomeApi
import com.example.motherload.data.api.InventoryApi
import com.example.motherload.data.api.ProfileApi
import com.example.motherload.data.callback.ConnexionCallback
import com.example.motherload.data.callback.HomeCallback
import com.example.motherload.data.callback.InventoryCallback
import com.example.motherload.data.callback.ProfilCallback

object Repository {
    private val TAG: String = "Repo"
    private var session: Long = -1
    private var signature: Long = -1

    fun getConnected(login: String, password: String, callback: ConnexionCallback){
        ConnexionApi.getConnected(login, password, callback)
    }
    fun deplacement(latitude:Double, longitude:Double, callback: HomeCallback){
        getSessionSignature()
        HomeApi.deplacement(session, signature, latitude, longitude, callback)
    }
    fun creuser(latitude: Double, longitude: Double, callback: HomeCallback){
        getSessionSignature()
        HomeApi.creuser(session, signature, latitude, longitude, callback)
    }
    fun getStatus(callback: InventoryCallback){
        getSessionSignature()
        InventoryApi.getStatus(session, signature, callback)
    }
    fun getItems(items: List<Item>, callback: InventoryCallback){
        getSessionSignature()
        InventoryApi.getItems(session, signature, items, callback)
    }
    fun upgradePickaxe(pickaxeLevel: Int, callback: InventoryCallback){
        getSessionSignature()
        InventoryApi.upgradePickaxe(session, signature, pickaxeLevel, callback)
    }
    fun recipePickaxe(callback: InventoryCallback){
        getSessionSignature()
        InventoryApi.recipePickaxe(session, signature, callback)
    }
    fun changerPseudo(pseudo: String, callback: ProfilCallback){
        getSessionSignature()
        ProfileApi.changerPseudo(session, signature, pseudo, callback)
    }
    fun resetUser(callback: ProfilCallback) {
        getSessionSignature()
        ProfileApi.resetUser(session, signature, callback)
    }


    private fun getSessionSignature() {
        val sharedPreferences = MotherLoad.instance.getSharedPreferences("Connexion", Context.MODE_PRIVATE)
        session = sharedPreferences.getLong("SessionId", -1)
        signature = sharedPreferences.getLong("Signature", -1)
    }


}