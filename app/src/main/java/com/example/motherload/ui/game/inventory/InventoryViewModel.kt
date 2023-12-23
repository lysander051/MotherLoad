package com.example.motherload.ui.game.inventory
import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.motherload.data.callback.InventoryCallback
import com.example.motherload.data.Item
import com.example.motherload.data.Repository
import com.example.motherload.data.callback.ItemCallback

class InventoryViewModel(var invRepo: Repository): ViewModel() {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getStatus(callback: InventoryCallback, activity: Activity){
        invRepo.getStatus(callback, activity)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getItems(item: List<Item>, callback: ItemCallback, activity: Activity){
        invRepo.getItems(item, callback, activity)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun upgradePickaxe(pickaxeLevel: Int, callback: InventoryCallback, activity: Activity){
        invRepo.upgradePickaxe(pickaxeLevel+1, callback, activity)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun recipePickaxe(callback: InventoryCallback, activity: Activity) {
        invRepo.recipePickaxe(callback, activity)
    }
}