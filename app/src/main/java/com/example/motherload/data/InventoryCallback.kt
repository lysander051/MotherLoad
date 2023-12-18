package com.example.motherload.data

interface InventoryCallback {
    fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>)
    fun getItems(itemDescription: MutableList<ItemDescription>)
    fun upgradePickaxe()
    fun recipePickaxe(recipe: MutableMap<String, List<Item>>)
}