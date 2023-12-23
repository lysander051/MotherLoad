package com.example.motherload.data.callback

import com.example.motherload.data.Item

interface ShopCallback {
    fun getMarketItems(items: List<Triple<Int, Item, Int>>)
    fun getInventory(items: MutableList<Item>)
    fun buyItem()
    fun sellItem()
    fun erreur()
}