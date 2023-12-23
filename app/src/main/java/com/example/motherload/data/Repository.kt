package com.example.motherload.data

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.motherland.MotherLoad
import com.example.motherload.data.local.AppDatabase
import com.example.motherload.data.api.ConnexionApi
import com.example.motherload.data.api.HomeApi
import com.example.motherload.data.api.InventoryApi
import com.example.motherload.data.api.ItemApi
import com.example.motherload.data.api.ProfileApi
import com.example.motherload.data.api.ShopApi
import com.example.motherload.data.callback.ConnexionCallback
import com.example.motherload.data.callback.HomeCallback
import com.example.motherload.data.callback.InventoryCallback
import com.example.motherload.data.callback.ItemCallback
import com.example.motherload.data.callback.ProfilCallback
import com.example.motherload.data.callback.ShopCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Repository private constructor(private val motherLoad: MotherLoad) {
    private val TAG: String = "Repo"
    private var session: Long = -1
    private var signature: Long = -1
    private val itemDao = AppDatabase.getDatabase(MotherLoad.instance).itemDescriptionDao()

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

    fun deplacement(latitude: Double, longitude: Double, callback: HomeCallback, activity: Activity) {
        getSessionSignature()
        HomeApi.deplacement(session, signature, latitude, longitude, callback, activity)
    }

    fun creuser(latitude: Double, longitude: Double, callback: HomeCallback, activity: Activity) {
        getSessionSignature()
        HomeApi.creuser(session, signature, latitude, longitude, callback, activity)
    }

    fun getDepthHole(): Triple<Float, Float, Int> {
        return HomeApi.getDepthHole()
    }

    fun getStatus(callback: InventoryCallback, activity: Activity) {
        getSessionSignature()
        InventoryApi.getStatus(session, signature, callback, activity)
    }

    fun getItems(items: List<Item>, callback: ItemCallback, activity: Activity) {
        GlobalScope.launch(Dispatchers.IO) {
            val sharedPreferences = MotherLoad.instance.getSharedPreferences("Time", Context.MODE_PRIVATE)
            val lastReset = sharedPreferences.getLong("lastReset", 0)
            val currentTimeHours: Long = System.currentTimeMillis() / (60000 * 60)
            val itemsIds: MutableList<String> = ArrayList()

            val uniqueItemIds = items.distinctBy { it.id }
            val numberOfUniqueItems = uniqueItemIds.size

            for (element in items) {
                itemsIds.add(element.id)
            }
            Log.d(TAG, "lastreset $lastReset")
            Log.d(TAG, "currenttime $currentTimeHours")
            Log.d(TAG, "reset ${currentTimeHours - lastReset >= 24}")
            val itemsStockees: MutableList<ItemDescription> = itemDao.getItemsByIds(itemsIds)
            if(currentTimeHours - lastReset >= 1){
                itemDao.deleteAll()
                val sharedPreferences: SharedPreferences = MotherLoad.instance.getSharedPreferences("Time", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putLong("lastReset", currentTimeHours)
                editor.apply()
            }
            if ( numberOfUniqueItems == itemsStockees.size) {
                launch(Dispatchers.Main) {
                    Log.d(TAG, "get from Database")
                    callback.getItemsDescription(itemsStockees)
                }
            }else{
                Log.d(TAG, "update Database")
                launch(Dispatchers.Main) {
                    ItemApi.getItems(session, signature, items, motherLoad, callback, activity)
                }
            }
        }
    }

    fun upgradePickaxe(pickaxeLevel: Int, callback: InventoryCallback, activity: Activity) {
        getSessionSignature()
        InventoryApi.upgradePickaxe(session, signature, pickaxeLevel, callback, activity)
    }

    fun recipePickaxe(callback: InventoryCallback, activity: Activity) {
        getSessionSignature()
        InventoryApi.recipePickaxe(session, signature, callback, activity)
    }

    fun changerPseudo(pseudo: String, callback: ProfilCallback, activity: Activity) {
        getSessionSignature()
        ProfileApi.changerPseudo(session, signature, pseudo, callback, activity)
    }

    fun resetUser(callback: ProfilCallback, activity: Activity) {
        getSessionSignature()
        ProfileApi.resetUser(session, signature, callback, activity)
    }
    fun getArtifact(callback: ProfilCallback, activity: Activity){
        getSessionSignature()
        ProfileApi.getArtifact(session, signature, callback, activity)
    }

    fun getInventory(callback: ProfilCallback, activity: Activity){
        getSessionSignature()
        ProfileApi.getInventory(session, signature, callback, activity)
    }

    fun getMarketItems(callback: ShopCallback, activity: Activity) {
        getSessionSignature()
        ShopApi.getMarketItems(session, signature, callback, activity)
    }

    fun getInventory(callback: ShopCallback, activity: Activity) {
        getSessionSignature()
        ShopApi.getInventory(session, signature, callback, activity)
    }

    fun buyItem(order_id: Int, callback: ShopCallback, activity: Activity) {
        getSessionSignature()
        ShopApi.buyItem(session, signature, order_id, callback, activity)
    }

    fun sellItem(quantity: Int, id: String?, prix: Int, callback: ShopCallback, activity: Activity) {
        getSessionSignature()
        ShopApi.sellItem(session, signature, quantity, id, prix, callback, activity)
    }


    private fun getSessionSignature() {
        val sharedPreferences =
            MotherLoad.instance.getSharedPreferences("Connexion", Context.MODE_PRIVATE)
        session = sharedPreferences.getLong("SessionId", -1)
        signature = sharedPreferences.getLong("Signature", -1)
    }
}