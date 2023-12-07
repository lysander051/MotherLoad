package com.example.motherload.UI.Game

import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.motherland.MotherLoad
import com.example.motherload.Data.InventoryRepo
import com.example.motherload.Data.Item

class InventoryViewModel(var inventoryRepo: InventoryRepo): ViewModel() {
    val inventory : LiveData<List<Item>> = inventoryRepo.inventory
    fun UpdateInventory(session : Long?, signature : Long?){
        if (session != null && signature != null) {
            inventoryRepo.updateInventory(session, signature)
        }
    }

    fun detailItem(item : Item?){
        //TODO Affiche le détail de l'item
    }

}
