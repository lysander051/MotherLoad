package com.example.motherload.data

import android.content.Context
import android.util.Log
import androidx.annotation.experimental.R
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherland.MotherLoad
import com.example.motherload.Model.AppDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.concurrent.thread

class Repository private constructor(private val motherLoad: MotherLoad) {
    private val TAG: String = "Repo"
    private var session: Long = -1
    private var signature: Long = -1
    val dao = AppDatabase.getDatabase(MotherLoad.instance).itemDescriptionDao()

    companion object {
        private var repository: Repository? = null

        fun getInstance(motherLoad: MotherLoad): Repository {
            return repository ?: synchronized(this) {
                repository ?: Repository(motherLoad).also { repository = it }
            }
        }
    }


    fun getConnected(login: String, password: String, callback: ConnexionCallback) {
        ConnexionApi.getConnected(login, password, callback)
    }

    fun deplacement(latitude: Double, longitude: Double, callback: HomeCallback) {
        getSessionSignature()
        HomeApi.deplacement(session, signature, latitude, longitude, callback)
    }

    fun creuser(latitude: Double, longitude: Double, callback: HomeCallback) {
        getSessionSignature()
        HomeApi.creuser(session, signature, latitude, longitude, callback)
    }

    fun getItems(item: List<Item>, context: Context, callback: HomeCallback) {
        getSessionSignature()
        HomeApi.getItems(session, signature, item, context, callback)
    }

    fun getDepthHole(): Triple<Float, Float, Int> {
        return HomeApi.getDepthHole()
    }

    fun getStatus(callback: InventoryCallback) {
        getSessionSignature()
        InventoryApi.getStatus(session, signature, callback)
    }

    fun getItems(items: List<Item>, callback: InventoryCallback) {
        getSessionSignature()
        val itemsIds : MutableList<String> = ArrayList()
        for (element in items){
            itemsIds.add(element.id)
        }
        val itemsStockees : MutableList<ItemDescription> = dao.getItemsByIds(itemsIds)
        if (items.size == itemsIds.size){
            Log.d("BASE","J'ai get ça frérot")
            callback.getItems(itemsStockees)
        }
        else{
            Log.d("BASE", "Guigui bosse plus que la database")
            InventoryApi.getItems(session, signature, items, callback)
        }
    }

    fun upgradePickaxe(pickaxeLevel: Int, callback: InventoryCallback) {
        getSessionSignature()
        InventoryApi.upgradePickaxe(session, signature, pickaxeLevel, callback)
    }

    fun recipePickaxe(callback: InventoryCallback) {
        getSessionSignature()
        InventoryApi.recipePickaxe(session, signature, callback)
    }

    fun changerPseudo(pseudo: String, callback: ProfilCallback) {
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

    fun getItems(items: List<Item>, callback: ShopCallback) {
        getSessionSignature()
        ShopApi.getItems(session, signature, items, callback)
    }

    fun getInventory(callback: ShopCallback) {
        getSessionSignature()
        ShopApi.getInventory(session, signature, callback)
    }

    fun buyItem(order_id: Int, callback: ShopCallback) {
        getSessionSignature()
        ShopApi.buyItem(session, signature, order_id, callback)
    }

    fun sellItem(quantity: Int, id: String?, prix: Int, callback: ShopCallback) {
        getSessionSignature()
        ShopApi.sellItem(session, signature, quantity, id, prix, callback)
    }


    private fun getSessionSignature() {
        val sharedPreferences =
            MotherLoad.instance.getSharedPreferences("Connexion", Context.MODE_PRIVATE)
        session = sharedPreferences.getLong("SessionId", -1)
        signature = sharedPreferences.getLong("Signature", -1)
    }
}