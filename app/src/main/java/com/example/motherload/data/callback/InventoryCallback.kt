package com.example.motherload.data.callback

import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription

interface InventoryCallback {
    fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>)
    fun upgradePickaxe()
    fun recipePickaxe(recipe: MutableMap<String, List<Item>>)
    fun erreur(erreurId: Int)
}