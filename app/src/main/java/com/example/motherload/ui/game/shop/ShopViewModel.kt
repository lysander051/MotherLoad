package com.example.motherload.ui.game.shop

import androidx.lifecycle.ViewModel
import com.example.motherload.data.Item
import com.example.motherload.data.Repository
import com.example.motherload.data.callback.ShopCallback

class ShopViewModel(var shopRepo: Repository): ViewModel() {
    fun getMarketItems(callback: ShopCallback){
        shopRepo.getMarketItems(callback)
    }
    fun getItemsDescription(items: List<Item>, callback: ShopCallback){
        shopRepo.getItems(items,callback)
    }
    fun getInventory(callback: ShopCallback){
        shopRepo.getInventory(callback)
    }
    fun buyItem(order_id: Int, callback: ShopCallback){
        shopRepo.buyItem(order_id, callback)
    }

    fun sellItem(quantity: Int, id: String?, prix: Int, callback: ShopCallback) {
        shopRepo.sellItem(quantity,id,prix,callback)
    }
}