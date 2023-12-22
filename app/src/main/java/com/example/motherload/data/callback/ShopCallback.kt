package com.example.motherload.data.callback

import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription

interface ShopCallback {
    fun getMarketItems(items: List<Triple<Int, Item, Int>>)
    fun getItemsDescription(items: MutableList<ItemDescription>)
    fun getInventory(items: MutableList<Item>)
    fun buyItem()
    fun sellItem()
    fun erreur()
}