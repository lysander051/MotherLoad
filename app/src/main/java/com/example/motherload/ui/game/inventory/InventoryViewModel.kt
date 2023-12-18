package com.example.motherload.ui.game.inventory
import androidx.lifecycle.ViewModel
import com.example.motherload.data.InventoryCallback
import com.example.motherload.data.Item
import com.example.motherload.data.Repository

class InventoryViewModel(var invRepo: Repository): ViewModel() {
    fun getStatus(callback: InventoryCallback){
        invRepo.getStatus(callback)
    }

    fun getItems(item: List<Item>,callback: InventoryCallback){
        invRepo.getItems(item, callback)
    }

    fun upgradePickaxe(pickaxeLevel: Int, callback: InventoryCallback){
        invRepo.upgradePickaxe(pickaxeLevel+1, callback)
    }

    fun recipePickaxe(callback: InventoryCallback) {
        invRepo.recipePickaxe(callback)
    }
}