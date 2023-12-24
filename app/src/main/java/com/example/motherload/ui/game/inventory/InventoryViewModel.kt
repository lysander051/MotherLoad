package com.example.motherload.ui.game.inventory
import android.app.Activity
import androidx.lifecycle.ViewModel
import com.example.motherload.data.callback.InventoryCallback
import com.example.motherload.data.Item
import com.example.motherload.data.Repository
import com.example.motherload.data.callback.ItemCallback

/**
 * @param invRepo l'instance de repository
 */
class InventoryViewModel(private var invRepo: Repository): ViewModel() {
    /**
     * Transmet la requête de récupération du status du joueur au repo
     *
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun getStatus(callback: InventoryCallback, activity: Activity){
        invRepo.getStatus(callback, activity)
    }

    /**
     * Transmet la requête de récupération des détails des objets au repo
     *
     * @param item la liste de items
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun getItems(item: List<Item>, callback: ItemCallback, activity: Activity){
        invRepo.getItems(item, callback, activity)
    }

    /**
     * Transmet la requête d'amélioration de la pioche au repo
     *
     * @param pickaxeLevel le niveau actuel de la pioche
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun upgradePickaxe(pickaxeLevel: Int, callback: InventoryCallback, activity: Activity){
        invRepo.upgradePickaxe(pickaxeLevel+1, callback, activity)
    }

    /**
     * Transmet la requête de recettes de pioches au repo
     *
     * @param callback le callback de la requête
     * @param activity l'activité courante
     */
    fun recipePickaxe(callback: InventoryCallback, activity: Activity) {
        invRepo.recipePickaxe(callback, activity)
    }
}