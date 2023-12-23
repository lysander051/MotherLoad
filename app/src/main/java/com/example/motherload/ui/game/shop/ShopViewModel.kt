package com.example.motherload.ui.game.shop

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.motherload.data.Item
import com.example.motherload.data.Repository
import com.example.motherload.data.callback.ItemCallback
import com.example.motherload.data.callback.ShopCallback

class ShopViewModel(var shopRepo: Repository): ViewModel() {
    fun getMarketItems(callback: ShopCallback, activity: Activity){
        shopRepo.getMarketItems(callback, activity)
    }
    fun getItemsDescription(items: List<Item>, callback: ItemCallback, activity: Activity){
        shopRepo.getItems(items,callback, activity)
    }
    fun getInventory(callback: ShopCallback, activity: Activity){
        shopRepo.getInventory(callback, activity)
    }
    fun buyItem(order_id: Int, callback: ShopCallback, activity: Activity){
        shopRepo.buyItem(order_id, callback, activity)
    }

    fun sellItem(quantity: Int, id: String?, prix: Int, callback: ShopCallback, activity: Activity) {
        shopRepo.sellItem(quantity,id,prix,callback, activity)
    }
}