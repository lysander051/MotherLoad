package com.example.motherload.UI.Game
import androidx.lifecycle.ViewModel
import com.example.motherload.Data.HomeCallback
import com.example.motherload.Data.InventoryCallback
import com.example.motherload.Data.Repository

class InventoryViewModel(var invRepo: Repository): ViewModel() {
    fun getStatus(callback: InventoryCallback){
        invRepo.getStatus(callback)
    }
}