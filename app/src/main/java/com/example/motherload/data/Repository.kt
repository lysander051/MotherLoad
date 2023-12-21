package com.example.motherload.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherland.MotherLoad
import com.example.motherload.data.api.ConnexionApi
import com.example.motherload.data.api.HomeApi
import com.example.motherload.data.api.InventoryApi
import com.example.motherload.data.api.ProfileApi
import com.example.motherload.data.api.ShopApi
import com.example.motherload.data.callback.ConnexionCallback
import com.example.motherload.data.callback.HomeCallback
import com.example.motherload.data.callback.InventoryCallback
import com.example.motherload.data.callback.ProfilCallback
import com.example.motherload.data.callback.ShopCallback
import org.osmdroid.util.GeoPoint
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

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
    fun getItems(item: List<Item>, callback: HomeCallback){
        getSessionSignature()
        HomeApi.getItems(session, signature, item , callback)
    }
    fun getDepthHole(): Triple<Float, Float, Int> {
        return HomeApi.getDepthHole()
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
    fun getMarketItems(callback: ShopCallback) {
        getSessionSignature()
        ShopApi.getMarketItems(session, signature, callback)
    }
    fun getItems(items: List<Item>, callback: ShopCallback){
        getSessionSignature()
        ShopApi.getItems(session, signature, items, callback)
    }
    fun getInventory(callback: ShopCallback){
        getSessionSignature()
        ShopApi.getInventory(session, signature, callback)
    }
    fun buyItem(order_id: Int, callback: ShopCallback){
        getSessionSignature()
        ShopApi.buyItem(session, signature, order_id, callback)
    }
    fun sellItem(quantity: Int, id: String?, prix: Int, callback: ShopCallback) {
        getSessionSignature()
        ShopApi.sellItem(session, signature, quantity, id, prix, callback)
    }


    private fun getSessionSignature() {
        val sharedPreferences = MotherLoad.instance.getSharedPreferences("Connexion", Context.MODE_PRIVATE)
        session = sharedPreferences.getLong("SessionId", -1)
        signature = sharedPreferences.getLong("Signature", -1)
    }
}