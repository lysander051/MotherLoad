package com.example.motherload.ui.game.shop

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.motherload.data.Item
import com.example.motherload.data.Repository
import com.example.motherload.data.callback.ItemCallback
import com.example.motherload.data.callback.ShopCallback

/**
 * @param shopRepo l'instance de repository
 */
class ShopViewModel(var shopRepo: Repository): ViewModel() {
    /**
     * Transmet les informations de la requête récupérant les offres au repo
     *
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun getMarketItems(callback: ShopCallback, activity: Activity){
        shopRepo.getMarketItems(callback, activity)
    }

    /**
     * Transmet les informations de la requête récupérant le détail des items au repo
     *
     * @param items les items dont on veut le détail
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun getItemsDescription(items: List<Item>, callback: ItemCallback, activity: Activity){
        shopRepo.getItems(items,callback, activity)
    }

    /**
     * Transmet les informations de la requête récupérant l'inventaire au repo
     *
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun getInventory(callback: ShopCallback, activity: Activity){
        shopRepo.getInventory(callback, activity)
    }

    /**
     * Transmet les informations de la requête effectuant l'achat d'une offre au repo
     *
     * @param order_id l'id de l'offre
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun buyItem(order_id: Int, callback: ShopCallback, activity: Activity){
        shopRepo.buyItem(order_id, callback, activity)
    }

    /**
     * Transmet les informations de la requête créant une offre au repo
     *
     * @param quantity la quantité à vendre
     * @param id l'id de l'objet à vendre
     * @param prix le prix de l'offre créée
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun sellItem(quantity: Int, id: String?, prix: Int, callback: ShopCallback, activity: Activity) {
        shopRepo.sellItem(quantity,id,prix,callback, activity)
    }
}